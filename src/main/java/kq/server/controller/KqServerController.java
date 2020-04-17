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
import kq.server.util.RandomUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Controller
public class KqServerController {

    private static Logger logger = Logger.getLogger(KqServerController.class);

    @Autowired
    private MessageHandlerService messageHandlerService;
    @Autowired
    private AchievementMapper achievementMapper;
    @Autowired
    private StoryService storyService;
    @Autowired
    private CardService cardService;
    //会有错误提示但可以运行

    @ResponseBody
    @RequestMapping(value="/eventsReceice", method= RequestMethod.POST)
    public String eventsReceice(@RequestBody JSONObject body){
        logger.info("上报消息 "+ body.toJSONString());
        if(body.containsKey("sender") && body.containsKey("message")){
            System.out.printf("用户 [%s]%s 发送消息 %s \n", body.getJSONObject("sender").getString("card"), body.getJSONObject("sender").getString("nickname"), body.get("message"));
        }

        Message msg;
        try {
            msg = new Message(body, true);
            MessageHandler.dealMessage(msg);
        } catch (Exception e){
            return "";
        }

        if(!msg.needDeal()){
           return "{}";
        }

        return "{\n" +
                "    \"block\": true\n" +
                "}";
    }

    @PostConstruct
    public void init(){
        new MessageHandler(messageHandlerService).start();
        new MessageSender().start();
        new AchievementSender().start();
        Achievement.achievementList = achievementMapper.getAchievements();
        new RandomUtil().start();
        storyService.initStoryCache();
        CardShop.setShopCards(cardService.createShopCards());
    }
}
