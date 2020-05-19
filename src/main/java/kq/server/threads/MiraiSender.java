package kq.server.threads;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.MiraiMessage;
import kq.server.service.MiraiMessageHandlerService;
import kq.server.service.MiraiMessageSenderService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForReadableInstant;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import static kq.server.util.HttpUtil.*;

import java.util.LinkedList;

public class MiraiSender extends Thread {

    private MiraiMessageSenderService miraiMessageSenderService;
    private static long lastSessionTime;
    private static long keepAliveTime;
    private static long lastCleanSession = System.currentTimeMillis();
    private static Logger logger = Logger.getLogger(MiraiSender.class);

    public MiraiSender(){
    }

    public MiraiMessageSenderService getMiraiMessageSenderService() {
        return miraiMessageSenderService;
    }

    public void setMiraiMessageSenderService(MiraiMessageSenderService miraiMessageSenderService) {
        this.miraiMessageSenderService = miraiMessageSenderService;
    }

    @Override
    public void run(){
        while (true){
            try {
                Thread.sleep(200);
                if((System.currentTimeMillis() - lastSessionTime) > 10*60*1000){
                    miraiMessageSenderService.checkSession();
                    lastSessionTime = System.currentTimeMillis();
                }
                if((System.currentTimeMillis() - keepAliveTime) > 25*60*1000){
                    miraiMessageSenderService.keepAlive();
                    keepAliveTime = System.currentTimeMillis();
                }
                if((System.currentTimeMillis() - lastCleanSession) > 12*60*60*1000){
                    miraiMessageSenderService.cleanSession();
                    lastCleanSession = System.currentTimeMillis();
                }
            } catch (Exception e){
                logger.error(e);
            }
        }
    }

}
