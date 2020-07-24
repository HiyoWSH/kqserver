package kq.server.controller;

import kq.server.bean.Achievement;
import kq.server.bean.rss.Houkai3RSS;
import kq.server.config.Configuation;
import kq.server.mapper.AchievementMapper;
import kq.server.mapper.RSSMapper;
import kq.server.rss.RSSpush;
import kq.server.service.*;
import kq.server.threads.AchievementSender;
import kq.server.threads.MessageHandler;
import kq.server.threads.MessageSender;
import kq.server.threads.MiraiSender;
import kq.server.util.CardShop;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Controller
public class KqControlController {

    private static Logger logger = Logger.getLogger(KqControlController.class);

    @Autowired
    private MessageHandlerService messageHandlerService;
    @Autowired
    private AchievementMapper achievementMapper;
    @Autowired
    private StoryService storyService;
    @Autowired
    private CardService cardService;
    @Autowired
    ImageService imageService;
    @Autowired
    MiraiMessageSenderService miraiMessageSenderService;
    @Autowired
    private RSSMapper rssMapper;

    @PostConstruct
    public void controlInit(){
        System.out.println("###key###" + Configuation.authKey);
        System.out.println("###key###" + Configuation.getRunMode());
        System.out.println("###key###" + Configuation.getMe());
        System.out.println("###key###" + Configuation.getMes());
        System.out.println("###key###" + Configuation.getMiraiserver());

        if(StringUtils.equals(Configuation.getRunMode(), "Mirai")){
            miraiInit();
        } else if(StringUtils.equals(Configuation.getRunMode(), "KQ")){
            kqInit();
        }
        init();
    }

    /**
     * mirai 初始化
     */
    private void miraiInit() {
        MiraiSender sender = new MiraiSender();
        sender.setMiraiMessageSenderService(miraiMessageSenderService);
        sender.start();
        try{
            imageService.initImageCache();
        } catch (Exception e) {
            logger.info(e);
        }

        RSSpush rsSpush = new RSSpush(rssMapper, miraiMessageSenderService);
        Map map = new HashMap();
        map.put("host", "rsshub.app");
        rsSpush.addRss(new Houkai3RSS("崩坏3rss", "http://rsshub.app.cdn.cloudflare.net/bilibili/user/dynamic/27534330",
                367896221, map, ""));
        rsSpush.start();
    }

    /**
     * kq初始化
     */
    private void kqInit(){
        new MessageHandler(messageHandlerService).start();
        new MessageSender().start();
        new AchievementSender().start();
    }

    /**
     * 通用初始化
     */
    private void init(){
        Achievement.achievementList = achievementMapper.getAchievements();
        new RandomUtil().start();
        CardShop.setShopCards(cardService.createShopCards());

        try{
//            storyService.initStoryCache();
        } catch (Exception e){
            logger.info(e);
        }
    }
}
