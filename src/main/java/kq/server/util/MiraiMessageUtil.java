package kq.server.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.MiraiMessage;
import kq.server.bean.User;
import kq.server.service.MiraiMessageSenderService;
import kq.server.threads.MiraiSender;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;

public class MiraiMessageUtil {

    public static ThreadLocal<JSONObject> miraiMessageThreadLocal = new ThreadLocal<JSONObject>();

    public static boolean isAtMe(JSONObject body, long me, String mes){
        if(body.containsKey("messageChain")){
            JSONArray messageChain = body.getJSONArray("messageChain");
            for (int i = 0; i < messageChain.size(); i++) {
                JSONObject message = messageChain.getJSONObject(i);
                if(message.containsKey("type") && "At".equals(message.getString("type"))
                        && message.containsKey("target") && String.valueOf(me).equals(message.getString("target"))){
                    return true;
                }
                if(StringUtils.isNotBlank(mes)
                        && message.containsKey("type") && "Plain".equals(message.getString("type"))
                        && message.containsKey("text") && message.getString("text").contains(mes)){
                    return true;
                }
            }
        }

        return false;
    }

    public static String getType(JSONObject body){
        if(body.containsKey("type")){
            return body.getString("type");
        }
        return "";
    }

    public static String getText(JSONObject body){
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        if(body.containsKey("messageChain")){
            JSONArray messageChain = body.getJSONArray("messageChain");
            for (int i = 0; i < messageChain.size(); i++) {
                JSONObject message = messageChain.getJSONObject(i);
                if(message.containsKey("type") && "Plain".equals(message.getString("type"))
                        && message.containsKey("text")){
                    if(!isFirst){
                        builder.append("\n");
                    }
                    builder.append(message.get("text"));
                    isFirst = false;
                }
            }
        }

        return builder.toString().trim();
    }

    public static String getImage(JSONObject body){
        StringBuilder builder = new StringBuilder();
        if(body.containsKey("messageChain")){
            JSONArray messageChain = body.getJSONArray("messageChain");
            for (int i = 0; i < messageChain.size(); i++) {
                JSONObject message = messageChain.getJSONObject(i);
                if(message.containsKey("type") && "Image".equals(message.getString("type"))
                        && message.containsKey("imageId")){
                    return message.getString("imageId");
                }
            }
        }

        return builder.toString().trim();
    }

    public static long getTarget(JSONObject body){
        String type = getType(body);
        switch (type) {
            case "GroupMessage":
                return body.getJSONObject("sender").getJSONObject("group").getLong("id");
            case "FriendMessage":
                return body.getJSONObject("sender").getLong("id");
        }
        return 0L;
    }

    public static long getAtTarget(JSONObject body){
        String type = getType(body);
        switch (type) {
            case "GroupMessage":
                return body.getJSONObject("sender").getLong("id");
        }
        return 0L;
    }

    public static JSONObject makeTargetJson(JSONArray messages, String session, long target){
        JSONObject targetJson = new JSONObject();
        targetJson.put("target", target);
        targetJson.put("sessionKey", session);
        targetJson.put("messageChain", messages);
        return targetJson;
    }

    public static MultiValueMap<String, Object> popHeaders(String session, String type, File img) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
//        FileSystemResource f = new FileSystemResource(img);
//        try {
//            byte[] bytesArray = new byte[(int) img.length()];
//
//            FileInputStream fis = new FileInputStream(img);
//            fis.read(bytesArray); //read file into bytes[]
//            fis.close();
//
//            ByteArrayResource contentsAsResource = new ByteArrayResource(bytesArray) {
//                @Override
//                public String getFilename() {
//                    return "img";
//                }
//            };
//
//            map.add("sessionKey", session);
//            map.add("type", type);
//            map.add("img", contentsAsResource);
//        } catch (Exception e){e.printStackTrace();}
        //.....
        map.add("sessionKey", session);
        map.add("type", type);
        map.add("img", img);
        return map;
    }

    public static JSONArray createMessageTextChain(String commandRes) {
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();
        json.put("type","Plain");
        json.put("text",commandRes);
        array.add(json);
        return array;
    }

    public static JSONArray createMessageImageChain(String imageId) {
        JSONArray array = new JSONArray();
        JSONObject json = new JSONObject();
        json.put("type","Image");
        json.put("imageId",imageId);
        array.add(json);
        return array;
    }
}
