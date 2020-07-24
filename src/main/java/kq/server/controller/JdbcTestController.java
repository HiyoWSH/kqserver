package kq.server.controller;

import kq.server.bean.Achievement;
import kq.server.bean.Card;
import kq.server.bean.rss.Houkai3RSS;
import kq.server.bean.rss.RSS;
import kq.server.bean.User;
import kq.server.mapper.AchievementMapper;
import kq.server.mapper.CardMapper;
import kq.server.mapper.RSSMapper;
import kq.server.mapper.UserMapper;
import kq.server.rss.RSSpush;
import kq.server.service.MiraiMessageSenderService;
import kq.server.util.FileOperate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class JdbcTestController {

    private static final Logger logger = Logger.getLogger(JdbcTestController.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private AchievementMapper achievementMapper;
    @Autowired
    private MiraiMessageSenderService miraiMessageSenderService;
    @Autowired
    private RSSMapper rssMapper;

//    @PostConstruct
    public void test(){
        List list;
        User user = new User();
        user.setUser_id(463832436);
        userMapper.insertUser(user);
        user = userMapper.getUser(463832436);
        list = userMapper.getUsers();
        list = cardMapper.getCards();
        list = cardMapper.getUserCards(463832436);
        System.out.println("test finished");
    }

    @ResponseBody
    @GetMapping("/addCardFromFile")
    public String addCardFromFile(){
        String filePath = "D:\\card\\cards.txt";
        List<String> cardsStr = FileOperate.readFileToStringList(filePath, "utf-8");
        for(String srt:cardsStr){
            String[] value = srt.split("@@@");
            if(value.length == 3){
                Card card = new Card();
                card.setCard_name(value[0]);
                card.setCard_description(value[1]);
                card.setRare(value[2]);
                cardMapper.insertCard(card);
                logger.info("insert card " + card.getCard_name());
            }
        }
        return "ok";
    }

    @ResponseBody
    @GetMapping("/printCardFromDb")
    public String printCardFromDb(){
        for(Card c:cardMapper.getCards()){
            System.out.printf("%s@@@%s@@@%s\n", c.getCard_name(), c.getCard_description(), c.getRare());
        }
        return "ok";
    }

    @ResponseBody
    @GetMapping("/printAchievementFromDb")
    public String printAchievementFromDb(){
        for(Achievement achievement:Achievement.achievementList){
            System.out.printf("%s@@@%s@@@%s@@@%d\n", achievement.getAchievement_name(), achievement.getNeeded(), achievement.getDescription(),achievement.getNeed_count());
        }
        return "ok";
    }

    @ResponseBody
    @GetMapping("/reloadAchievement")
    public String reloadAchievement(){
        Achievement.achievementList = achievementMapper.getAchievements();
        return "ok";
    }

    @ResponseBody
    @GetMapping("/uploadTest")
    public String uploadTest(){
        String path = "F:\\setudir\\fromNet38312979_p0.jpg";
        String res = miraiMessageSenderService.uploadImage(path);
        return res;
    }

    @ResponseBody
    @GetMapping("/rssTest")
    public String rssTest(){
        Map map = new HashMap();
        map.put("host", "rsshub.app");

        RSSpush rsSpush = new RSSpush(rssMapper, miraiMessageSenderService);
        RSS rss = new Houkai3RSS("测试rss", "http://rsshub.app.cdn.cloudflare.net/bilibili/user/dynamic/27534330",
                367896221, map, "");

        String rssres = rsSpush.getRssRes(rss);
        logger.info("获得订阅内容" + rssres.substring(0, 20));
        if(rsSpush.hasUpdate(rss, rssres)){
            try {
                rsSpush.dopush(rss, rssres);
            } catch (Exception e){
                logger.error(e);
            }
        }
        return "res";
    }
}
