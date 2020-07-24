package kq.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.MiraiMessage;
import kq.server.config.Configuation;
import kq.server.service.MiraiMessageHandlerService;
import kq.server.service.MiraiMessageSenderService;
import kq.server.util.MiraiMessageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static kq.server.util.HttpUtil.createHttpEntity;
import static kq.server.util.HttpUtil.post;

@Service
public class MiraiMessageSenderServiceImpl implements MiraiMessageSenderService {

    @Autowired
    private MiraiMessageHandlerService miraiMessageHandlerService;

    private static String session;

    private static final String GROUP_MESSAGE_URL = "/sendGroupMessage";
    private static final String FRIEND_MESSAGE_URL = "/sendFriendMessage";
    private static final String IMAGE_MESSAGE_URL = "/sendImageMessage";

    private static Logger logger = Logger.getLogger(MiraiMessageSenderServiceImpl.class);

    static ThreadLocal<LinkedList<String>> imageSendWaitList = new ThreadLocal<LinkedList<String>>();

    @Override
    public void sendMessage(MiraiMessage message) {
        checkSession();
        message.getBody().put("sessionKey", session);
        if(message.getAtTarget() > 0){
            JSONObject json = new JSONObject();
            json.put("type", "At");
            json.put("target", message.getAtTarget());
            message.getBody().getJSONArray("messageChain").add(0, json);
        }
        String postRes = post(getTargetUrl(message), createHttpEntity(message.getBody()));
        message.setMsgres(postRes);

        try {
            while (imageSendWaitList.get() != null && imageSendWaitList.get().size() > 0) {
                String imageUrl = imageSendWaitList.get().pop();
                if(StringUtils.isNotBlank(imageUrl)) {
                    miraiMessageHandlerService.sendImage(message.getBody(), imageUrl);
                }
            }
        } catch (Exception e){

            logger.error(e);
        }
    }

    @Override
    public void sendImageWait(String imgUrl) {
        if(imageSendWaitList.get()==null){
            imageSendWaitList.set(new LinkedList<>());
        }
        imageSendWaitList.get().add(imgUrl);
    }

    @Override
    public String sendMessageGetRes(MiraiMessage message){
        checkSession();
        message.getBody().put("sessionKey", session);
        String postRes = post(getTargetUrl(message), createHttpEntity(message.getBody()));
        return postRes;
    }

    private static String getTargetUrl(MiraiMessage message) {
        String miraiServer = Configuation.getMiraiserver();
        switch (message.getType()){
            case "FriendMessage":
                return miraiServer + FRIEND_MESSAGE_URL;
            case "GroupMessage":
                return miraiServer + GROUP_MESSAGE_URL;
            case "ImageMessage":
                return miraiServer + IMAGE_MESSAGE_URL;
        }
        return null;
    }

    @Override
    public void keepAlive() {
        JSONObject json = new JSONObject();
        json.put("sessionKey", session);
        json.put("target", Configuation.getMe());
        JSONArray array = new JSONArray();
        JSONObject msg1 = new JSONObject();
        array.add(msg1);
        msg1.put("type", "Plain");
        msg1.put("text", "keep alive");
        json.put("messageChain", array);
        MiraiMessage message = new MiraiMessage();
        message.setBody(json);
        message.setType("FriendMessage");
        sendMessage(message);
    }

    @Override
    public void checkSession() {
        if(session == null){
            createNewSession();
        }
        JSONObject res = JSONObject.parseObject(checkSessionOnly());
        if(res.containsKey("code") && res.getInteger("code") != 0){
            logger.warn(String.format("check session failed and get new one , res = %s", res.toJSONString()));
            createNewSession();
            checkSessionOnly();
        }
    }

    @Override
    public String checkSessionOnly() {
        String url = Configuation.getMiraiserver() + "/verify";
        JSONObject json = new JSONObject();
        json.put("sessionKey", session);
        json.put("qq", Configuation.getMe());
        return post(url, createHttpEntity(json));
    }

    @Override
    public void createNewSession() {
        String url = Configuation.getMiraiserver() + "/auth";
        JSONObject json = new JSONObject();
        json.put("authKey", Configuation.getAuthKey());
        JSONObject res = JSONObject.parseObject(post(url, createHttpEntity(json)));
        if(res.containsKey("code") && res.getInteger("code") == 0){
            session = res.getString("session");
            logger.info(String.format("更新新session %s", session));
        } else {
            logger.error(String.format("获取session失败，res=%s", res.toString()));
        }
    }

    @Override
    public void cleanSession(){
        String url = Configuation.getMiraiserver() + "/release";
        JSONObject json = new JSONObject();
        json.put("sessionKey", session);
        json.put("qq", Configuation.getMe());
        post(url, createHttpEntity(json));
        createNewSession();
    }

    @Override
    public void recallMessage(int id){
        String url = Configuation.getMiraiserver() + "/recall";
        JSONObject json = new JSONObject();
        json.put("sessionKey", session);
        json.put("target", id);
        post(url, createHttpEntity(json));
    }

    @Override
    public String sendImageMessage(JSONObject json){
        MiraiMessage message = new MiraiMessage();
        message.setType("ImageMessage");
        message.setBody(json);
        return sendMessageGetRes(message);
    }

    @Override
    public void sendMessage(String type, JSONObject json){
        sendMessage(type, json, 0);
    }

    @Override
    public void sendMessage(String type, JSONObject json, long targetId){
        MiraiMessage message = new MiraiMessage();
        message.setType(type);
        message.setBody(json);
        message.setAtTarget(targetId);
        sendMessage(message);
    }

    @Override
    public String uploadImage(String imgPath){
        String url = Configuation.getMiraiserver() + "/uploadImage";
        File file = new File(imgPath);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        MultiValueMap<String, Object> params = MiraiMessageUtil.popHeaders(session, "group", new File(imgPath));
////        System.out.println(JSONObject.toJSONString(params));
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(params, headers), String.class);
//        return response.getBody();
        Map<String,String> params = new HashMap<>();
        params.put("sessionKey", session);
        params.put("type", "group");

        String result= null;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);// 请求地址
        MultipartEntityBuilder meb = MultipartEntityBuilder.create();
        ContentType strContent = ContentType.create("application/json", Charset.forName("UTF-8"));
        if(file.exists()){
            meb.addBinaryBody("img", file);
        }
        params.forEach((k,v)->{
            meb.addTextBody(k, v, strContent);// 参数

        });
        org.apache.http.HttpEntity httpEntity = meb.build();
        httpPost.setEntity(httpEntity);
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            HttpResponse httpResponse = httpClient.execute(httpPost);
            InputStream inputStream = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            result = URLDecoder.decode(sb.toString(),"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("upload image res " + result);
        return  result;
    }

}
