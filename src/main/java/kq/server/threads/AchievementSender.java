package kq.server.threads;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Message;
import kq.server.enums.MessageTypeEnum;
import org.apache.log4j.Logger;

import java.util.LinkedList;

import static kq.server.util.MessageUtil.addJsonMessageWithEnter;
import static kq.server.util.MessageUtil.getResBase;

public class AchievementSender extends Thread {
    private static Logger logger = Logger.getLogger(AchievementSender.class);
    private static LinkedList<Message> messageList = new LinkedList<Message>();

    public static void sendAchievement(Message message){
        messageList.add(message);
    }

    public static void sendAchievement(Achievement achievement, Message message){
        Message achievementMessage = new Message(message.getBody(), true);
        JSONObject resjson = getResBase(achievementMessage);
        addJsonMessageWithEnter(resjson, "获得成就[" + achievement.getAchievement_name() + "]");
        addJsonMessageWithEnter(resjson,"可输入命令【查看成就】查看已获得的成就");
        achievementMessage.setResbody(resjson);
        messageList.add(achievementMessage);
    }

    @Override
    public void run(){
        while (true){
            try {
                if (messageList.size() > 0) {
                    Message message = messageList.pop();
                    Thread.sleep(3000);
                    MessageSender.sendMessage(message);
                }

                Thread.sleep(200);
            } catch (Exception e){
                logger.error(e);
            }
        }
    }
}
