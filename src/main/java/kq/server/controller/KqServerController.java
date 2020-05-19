package kq.server.controller;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Message;
import kq.server.mapper.AchievementMapper;
import kq.server.mapper.BlogMapper;
import kq.server.service.*;
import kq.server.threads.AchievementSender;
import kq.server.threads.MessageHandler;
import kq.server.threads.MessageSender;
import kq.server.util.CardShop;
import kq.server.util.MessageUtil;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.TimeZone;

@Controller
public class KqServerController {

    private static Logger logger = Logger.getLogger(KqServerController.class);

    @Autowired
    private NormalService normalService;
    //会有错误提示但可以运行

    @ResponseBody
    @RequestMapping(value="/eventsReceice", method= RequestMethod.POST)
    public String eventsReceice(@RequestBody JSONObject body){
        logger.info("上报消息 "+ body.toJSONString());
        if(body.containsKey("sender") && body.containsKey("message")){
            System.out.printf("用户 [%s]%s 发送消息 %s \n", body.getJSONObject("sender").getString("card"), body.getJSONObject("sender").getString("nickname"), body.get("message"));
        }

        boolean isDebug = false;

        Message msg;
        try {

//            if(body.containsKey("sub_type") && StringUtils.equals("approve", body.getString("sub_type"))){
//
//            } else {
                msg = new Message(body, true);
                if(isDebug){
                    msg.setResbody(MessageUtil.getNormalRes(msg, "维护中..."));
                    MessageSender.sendMessage(msg);
                    return "";
                }
                MessageHandler.dealMessage(msg);
//            }
        } catch (Exception e){
            return "";
        }

        // 随机自动回复
        if(!msg.needDeal()){
            if (!msg.getRaw_message().contains("[CQ")) {
                if (RandomUtil.getNextInt(100) > 96) {
                    try {
                        JSONObject resjson = MessageUtil.getResBaseNoAt(msg);
                        MessageUtil.addJsonMessage(resjson, normalService.getTuLingRes(msg.getCommand()));
                        msg.setResbody(resjson);
                        MessageSender.sendMessage(msg);
                    } catch (Exception e){
                        logger.warn(e);
                    }
                }
            }
        }

        return "{\n" +
                "    \"block\": true\n" +
                "}";
    }

    private void sendMaintainceMessage(){
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        String msg = String.format("%s %d年%月%d日 %s\n%s",
                "栗小栗", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                "更新",
                ""
                );
    }
}
