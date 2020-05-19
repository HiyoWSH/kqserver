package kq.server.util;

import kq.server.threads.MessageSender;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class HttpUtil {

    private static Logger logger = Logger.getLogger(HttpUtil.class);
    private static RestTemplate restTemplate = new RestTemplate();

    public static HttpEntity createHttpEntity(Object body){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity requestEntity=new HttpEntity(body, headers);
        return requestEntity;
    }

    public static String post(String url, HttpEntity entity){
        try {
            logger.info("post for , url = " + url);
            logger.info("post for , body = " + entity.getBody());
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
