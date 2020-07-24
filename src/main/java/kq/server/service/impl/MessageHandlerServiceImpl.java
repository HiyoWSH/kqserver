package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import kq.server.bean.Message;
import kq.server.bean.Tuling;
import kq.server.bean.User;
import kq.server.gm.trpgmode.mode1.HatuneRoomMode;
import kq.server.gm.trpgmode.mode2.ZonbiRoomMode;
import kq.server.mapper.AchievementMapper;
import kq.server.mapper.CardMapper;
import kq.server.mapper.TulingMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.*;
import kq.server.threads.MessageSender;
import kq.server.threads.TrpgRunner;
import kq.server.util.DealRegexLib;
import kq.server.util.MessageUtil;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static kq.server.util.TimeUtil.getTodayStartTime;

@Service
public class MessageHandlerServiceImpl implements MessageHandlerService {

    private static Logger logger = Logger.getLogger(MessageHandlerServiceImpl.class);

    @Autowired
    CardService cardService;
    @Autowired
    ChouqianService chouqianService;
    @Autowired
    AchievementService achievementService;
    @Autowired
    StoryService storyService;
    @Autowired
    UserService userService;
    @Autowired
    NormalService normalService;
    @Autowired
    UserMapper userMapper;
    @Autowired
    CardMapper cardMapper;
    @Autowired
    TulingMapper tulingMapper;
    @Autowired
    AchievementMapper achievementMapper;

    TrpgRunner trpgRunner;

    @Override
    public void doDeal(Message message) {
        String command = message.getCommand();
        try {
            if(trpgRunner != null && trpgRunner.isAlive()){
                for(User user:trpgRunner.getUserlist()){
                    if(user.getUser_id() == message.getUser_id()){
                        trpgRunner.getUserOption().get(message.getUser_id()).add(message.getCommand());
                        return;
                    }
                }
            }

            JSONObject resjson = MessageUtil.getResBase(message);
            User user = userMapper.getUser(message.getUser_id());
            if(user == null){
                user = new User();
                user.setUser_id(message.getUser_id());
                userMapper.insertUser(user);
            }

            if(command.startsWith("功能") || command.startsWith("菜单") || command.startsWith("help") || command.startsWith("帮助")){
                MessageUtil.addJsonMessageWithEnter(resjson, normalService.doDealMenu());
            } else if(command.contains("抽签")){
                MessageUtil.addJsonMessageWithEnter(resjson, chouqianService.getChouqianRes(user));
            } else if(command.contains("抽卡")){
                MessageUtil.addJsonMessageWithEnter(resjson, cardService.doDealChouka(user, message.getCommand()));
            } else if(command.contains("查看卡牌") || command.contains("查看卡片")){
                MessageUtil.addJsonMessageWithEnter(resjson, cardService.doShowCard(user, message.getCommand()));
            } else if(command.contains("卡牌商店")){
                MessageUtil.addJsonMessageWithEnter(resjson, cardService.doShowCardShop(user));
            } else if(command.contains("交换卡牌")){
                MessageUtil.addJsonMessageWithEnter(resjson, cardService.doCardExchange(user, message.getCommand()));
            } else if(command.contains("资料")){
                MessageUtil.addJsonMessageWithEnter(resjson, userService.showUserInfo(user));
            } else if(command.contains("查看成就")){
                MessageUtil.addJsonMessageWithEnter(resjson, achievementService.doShowAchievement(user, command));
            } else if(command.contains("听故事") || command.contains("讲故事")) {
                MessageUtil.addJsonMessageWithEnter(resjson, storyService.doDealStory(command));
            } else if(command.contains("TRPG MODE1")){
                user.setName(message.getBody().getJSONObject("sender").getString("nickname"));
                if(trpgRunner != null && trpgRunner.isAlive()) {
                    trpgRunner.addUser(user);
                    resjson = MessageUtil.getNormalRes(message, String.format("%s 加入了模组，当前模组人数 %d人", user.getName(), trpgRunner.getUserlist().size()));
                } else {
                    trpgRunner = new TrpgRunner(message, new HatuneRoomMode(), user);
                    trpgRunner.start();
                    return;
                }
            } else if(command.contains("TRPG MODE2")){
                user.setName(message.getBody().getJSONObject("sender").getString("nickname"));
                if(trpgRunner != null && trpgRunner.isAlive()) {
                    trpgRunner.addUser(user);
                    resjson = MessageUtil.getNormalRes(message, String.format("%s 加入了模组，当前模组人数 %d人", user.getName(), trpgRunner.getUserlist().size()));
                } else {
                    trpgRunner = new TrpgRunner(message, new ZonbiRoomMode(), user);
                    trpgRunner.start();
                    return;
                }
            }  else if(command.contains("TRPG MODE")){
                resjson = MessageUtil.getNormalRes(message, "模组不存在");
            } else {
                MessageUtil.addJsonMessageWithEnter(resjson, normalService.getNormalResStr(user, command));
            }
            message.setResbody(resjson);
            //发送
            MessageSender.sendMessage(message);

            // 检测成就
            cardService.checkAchievement(user, message);
        } catch (Exception e){
            e.printStackTrace();
            logger.error("模组内部错误", e);
        }
    }

    public static void main(String argv[]) {

    }
}
