package kq.server.threads;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.enums.MessageTypeEnum;
import kq.server.service.MessageHandlerService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;

/**
 * 发送消息线程
 */
public class MessageSender extends Thread {

    private static Logger logger = Logger.getLogger(MessageSender.class);
    static LinkedList<Message> messageList = new LinkedList<Message>();

    private RestTemplate restTemplate;
    private String groupurl = "http://127.0.0.1:5700/send_group_msg";
    private String privateurl = "http://127.0.0.1:5700/send_private_msg";

    public MessageSender() {
        this.restTemplate = new RestTemplate();
    }

    public static void sendMessage(Message message){
        messageList.add(message);
    }

    public static void sendMessage(JSONObject resjson, MessageTypeEnum type){
        Message message = new Message();
        message.setResbody(resjson);
        message.setMessage_type(type);
        messageList.add(message);
    }

    @Override
    public void run(){
        while (true){
            try {
                if (messageList.size() > 0) {
                    Message message = messageList.pop();
                    if(MessageTypeEnum.PRIVATE == message.getMessage_type()){
                        post(privateurl, createHttpEntity(message.getResbody()));
                    } else if(MessageTypeEnum.GROUP == message.getMessage_type()) {
                        post(groupurl, createHttpEntity(message.getResbody()));
                    }
                }

                Thread.sleep(200);
            } catch (Exception e){
                logger.error(e);
            }
        }
    }


    private HttpEntity createHttpEntity(Object body){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity requestEntity=new HttpEntity(body, headers);
        return requestEntity;
    }

    private String post(String url, HttpEntity entity){
        try {
            logger.info("post for systemres, url = " + url);
            String result = restTemplate.postForObject(url, entity, String.class);
            logger.info("post for systemres, result = " + result);
            return result;
        } catch (Exception e) {
            logger.error(url, e);
            e.printStackTrace();
            return "";
        }
    }
}
