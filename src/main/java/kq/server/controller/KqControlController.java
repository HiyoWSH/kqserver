package kq.server.controller;

import kq.server.bean.Achievement;
import kq.server.mapper.AchievementMapper;
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

@Controller
public class KqControlController {

    private static Logger logger = Logger.getLogger(KqControlController.class);
    public static String MESSAGE_SOURCE = "Mirai";//"KQ";//

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

    @PostConstruct
    public void controlInit(){
        if(StringUtils.equals(MESSAGE_SOURCE, "Mirai")){
            miraiInit();
        } else if(StringUtils.equals(MESSAGE_SOURCE, "KQ")){
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
        imageService.initImageCache();
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
            storyService.initStoryCache();
        } catch (Exception e){
            logger.info(e);
        }
    }
}
