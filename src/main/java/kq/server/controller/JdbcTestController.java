package kq.server.controller;

import kq.server.bean.Achievement;
import kq.server.bean.Card;
import kq.server.bean.User;
import kq.server.mapper.AchievementMapper;
import kq.server.mapper.CardMapper;
import kq.server.mapper.UserMapper;
import kq.server.util.FileOperate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.List;

@Controller
public class JdbcTestController {

    private static final Logger logger = Logger.getLogger(JdbcTestController.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CardMapper cardMapper;
    @Autowired
    private AchievementMapper achievementMapper;

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
}
