package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Card;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.CardMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.CardService;
import kq.server.threads.AchievementSender;
import kq.server.util.MessageUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static kq.server.util.TimeUtil.*;
import static kq.server.util.MessageUtil.*;

@Service
public class CardServiceImpl implements CardService {

    private static Logger logger = Logger.getLogger(CardServiceImpl.class);
    @Autowired
    UserMapper userMapper;
    @Autowired
    CardMapper cardMapper;

    @Override
    public JSONObject doGetCard(Message message, int count) {
        User user = userMapper.getUser(message.getUser_id());
        if(user == null){
            user = new User();
            user.setUser_id(message.getUser_id());
            userMapper.insertUser(user);
        }
        //超过
        if(user.getLast_get_card() == null || getTodayStartTime() - user.getLast_get_card().getTime() > 0){
            user.setCard_left(10);
        }
        JSONObject resjson = MessageUtil.getResBase(message);
        if(user.getCard_left() < count){
            MessageUtil.addJsonMessage(resjson, "\n今天的剩余抽卡次数是" + user.getCard_left() + "次哦，请不要太贪心");
            return resjson;
        }

        List<Card> getResCard = new ArrayList<>();
        MessageUtil.addJsonMessageWithEnter(resjson, "抽卡结果：");
        List<Card> allCards = cardMapper.getCards();
        List<Card> cards = allCards;
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < count; i++) {
            int r = random.nextInt(100);
            logger.info("[Chouka random1] = " + r);
            if(r >= 97){
                // SSR
                cards = getCards("SSR", allCards);
            } else if(r >= 87) {
                //SR
                cards = getCards("SR", allCards);
            } else if(r >= 72) {
                //R
                cards = getCards("R", allCards);
            } else if(r >= 50) {
                //N
                cards = getCards("N", allCards);
            } else {
                cards = getCards("", allCards);
            }
            if(cards.size() > 0) {
                int r2 = random.nextInt(cards.size());
                logger.info("[Chouka random2] = " + r);
                Card getC = cards.get(r2);
                MessageUtil.addJsonMessageWithEnter(resjson, (i+1) + "." + getC.getCard_name()+ "(" + getC.getRare() + ")");
                getResCard.add(getC);
            } else {
                MessageUtil.addJsonMessageWithEnter(resjson, (i+1) + ". 什么都没有抽到");
            }
        }

        List<Card> userCards = cardMapper.getUserCards(message.getUser_id());
        for(Card c:getResCard){
            boolean hasCard = false;
            for(Card uc:userCards){
                if(uc.getCard_id() == c.getCard_id()){
                    hasCard = true;
                    break;
                }
            }
            if(!hasCard){
                c.setUser_id(message.getUser_id());
                cardMapper.userGetCards(c);
                userCards.add(c);
            }
        }
        user.setCard_left(user.getCard_left() - count);
        user.setLast_get_card(new Date());
        checkAchievement(user, message);
        userMapper.updateUser(user);

        MessageUtil.addJsonMessageWithEnter(resjson, "输入“查看卡牌”查看已抽取到的卡牌");
        return resjson;
    }

    /**
     * 检查是否满足新的成就
     * @param user
     */
    private void checkAchievement(User user, Message message) {
        List<Card> userCards = cardMapper.getUserCards(user.getUser_id());
        for(Achievement achievement:Achievement.achievementList){
            if(!user.hasAchievement(achievement)) {
                int get = 0;
                String[] needed = achievement.getNeededArray();
                for (String need : needed) {
                    for (Card card : userCards) {
                        if (need.equals(card.getCard_name())) {
                            get++;
                            break;
                        }
                    }
                }
                if (get >= needed.length || (achievement.getNeed_count() > 0 && get >= achievement.getNeed_count())) {
                    user.addAchievement(achievement);
                    AchievementSender.sendAchievement(achievement, message);
                }
            }
        }
    }

    @Override
    public JSONObject doShowCard(Message message){
        String command = message.getCommand();
        try{
            String cardName = command.split("查看卡.")[1].trim();
            if(!"".equals(cardName)){
                List<Card> cards = cardMapper.getCards();
                JSONObject resjson = MessageUtil.getResBase(message);
                for(Card c:cards){
                    if(cardName.equals(c.getCard_name())){
                        MessageUtil.addJsonMessageWithEnter(resjson, "卡牌名称：" + cardName);
                        MessageUtil.addJsonMessageWithEnter(resjson, "稀有度：" + c.getRare());
                        MessageUtil.addJsonMessageWithEnter(resjson, JSONObject.parseObject(c.getCard_description()).getString("description"));
                        return resjson;
                    }
                }
                MessageUtil.addJsonMessageWithEnter(resjson, "未找到卡牌 " + cardName);
                return resjson;
            }
        }catch (Exception e){
            System.out.println("未输入卡牌名称");
        }

        int user_id = message.getUser_id();
        JSONObject resjson = MessageUtil.getResBase(message);
        List<Card> userCards = cardMapper.getUserCards(user_id);
        if(userCards.size() == 0){
            MessageUtil.addJsonMessageWithEnter(resjson, "你还没有获得认可卡牌呢，输入抽卡抽取");
            return resjson;
        }
        MessageUtil.addJsonMessageWithEnter(resjson, "你拥有的卡牌如下：");
        for(Card c:userCards){
            MessageUtil.addJsonMessageWithEnter(resjson, c.getCard_name() + "(" + c.getRare() + ")");
        }
        MessageUtil.addJsonMessageWithEnter(resjson, "输入“查看卡牌 卡牌名称” 查看卡牌详细信息");
        return resjson;
    }

    List<Card> getCards(String rare, List<Card> cards){
        List<Card> resCard = new ArrayList<>();
        Iterator<Card> it = cards.iterator();
        while (it.hasNext()){
            Card c = it.next();
            if(rare.equals(c.getRare())){
                resCard.add(c);
            }
        }
        return resCard;
    }

}
