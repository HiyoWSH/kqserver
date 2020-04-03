package kq.server.threads;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.enums.MessageTypeEnum;
import kq.server.service.AchievementService;
import kq.server.service.CardService;
import kq.server.service.ChouqianService;
import kq.server.util.MessageUtil;
import org.apache.log4j.Logger;
import org.junit.Test;
import java.util.LinkedList;
import java.util.Random;

public class MessageHandler extends Thread {

    CardService cardService;
    ChouqianService chouqianService;
    AchievementService achievementService;
    private static Logger logger = Logger.getLogger(MessageHandler.class);
    static LinkedList<Message> messageList = new LinkedList<Message>();

    public MessageHandler(CardService cardService, ChouqianService chouqianService, AchievementService achievementService) {
        this.cardService = cardService;
        this.chouqianService = chouqianService;
        this.achievementService = achievementService;
    }

    public static void dealMessage(Message message){
        messageList.add(message);
    }

    @Override
    public void run(){
        while (true){
            try {
                if (messageList.size() > 0) {
                    Message message = messageList.pop();
                    if (message.needDeal()) {
                        String command = message.getCommand();
                        doDeal(message, command);
                    }
                }

                Thread.sleep(200);
            } catch (Exception e){
                e.printStackTrace();
                logger.error(e);
            }
        }
    }

    private void doDeal(Message message, String command) {
        try {
            JSONObject resjson = null;
            if(command.contains("抽签")){
                resjson = chouqianService.getChouqianRes(message);
            } else if(command.contains("贴贴")){
                resjson = MessageUtil.getNormalRes(message, "蹭蹭~");
            } else if(command.contains("抱抱")){
                resjson = MessageUtil.getNormalRes(message, "抱~");
            } else if(command.contains("抽卡")){
                int count = 1;
                try{
                    System.out.println("command " + command);
                    count = Integer.parseInt(command.replaceAll("[^0-9]",""));
                } catch (Exception e){e.printStackTrace();}
                resjson = cardService.doGetCard(message, count);
            } else if(command.contains("查看卡牌") || command.contains("查看卡片")){
                resjson = cardService.doShowCard(message);
            } else if(command.contains("查看成就")){
                resjson = achievementService.doShowAchievement(message);
            } else {
                resjson = MessageUtil.getNormalRes(message);
            }
            message.setResbody(resjson);
            //发送
            MessageSender.sendMessage(message);
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e);
        }
    }

}
