package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Card;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.CardMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.CardService;
import kq.server.service.UserService;
import kq.server.threads.AchievementSender;
import kq.server.util.CardShop;
import kq.server.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static kq.server.util.MessageUtil.addJsonMessageWithEnter;
import static kq.server.util.TimeUtil.*;
import static kq.server.util.MessageUtil.*;
import static kq.server.util.RandomUtil.*;

@Service
public class CardServiceImpl implements CardService {

    private static Logger logger = Logger.getLogger(CardServiceImpl.class);
    @Autowired
    UserMapper userMapper;
    @Autowired
    CardMapper cardMapper;
    @Autowired
    UserService userService;

    /**
     * 抽卡 count 次
     * @param message
     * @param count
     * @return
     */
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
            addJsonMessage(resjson, "\n今天的剩余抽卡次数是" + user.getCard_left() + "次哦，请不要太贪心");
            return resjson;
        }

        List<Card> getResCard = new ArrayList<>();
        doChouka(getResCard, resjson, count);
        int getCoins = saveUserCard(user, getResCard, message, count);
        if(getCoins > 0){
            addJsonMessageWithEnter(resjson, "抽到的重复卡牌转换为硬币" + getCoins + "枚");
            addJsonMessageWithEnter(resjson, "当前拥有硬币 " + userMapper.getUser(message.getUser_id()).getCoins() +  "枚");
        }
        addJsonMessageWithEnter(resjson, "输入“查看卡牌”查看已抽取到的卡牌");
        return resjson;
    }

    /**
     * 查看卡牌
     * @param message
     * @return
     */
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

    /**
     * 抽卡命令入口
     * @param message
     * @return
     */
    @Override
    public JSONObject doDealChouka(Message message){
        int count = 1;
        try{
            System.out.println("command " + message.getCommand());
            count = Integer.parseInt(message.getCommand().replaceAll("[^0-9]",""));
        } catch (Exception e){e.printStackTrace();}
        return doGetCard(message, count);
    }

    /**
     * 卡牌商店
     * @param message
     * @return
     */
    @Override
    public JSONObject doShowCardShop(Message message) {
        JSONObject resjson = getResBase(message);
        addJsonMessageWithEnter(resjson, "今日可交换卡牌");
        List<Card> shopCards = CardShop.getShopCards();
        int index = 1;
        for (Card c:shopCards){
            addJsonMessageWithEnter(resjson,
                    index + "." + c.getCard_name() + " (" + c.getRare() + ")......" +
                            (140-20*index) + "硬币");
            index++;
        }
        addJsonMessageWithEnter(resjson,
                index + ".十连扭蛋券......20硬币");
        addJsonMessageWithEnter(resjson, "");
        addJsonMessageWithEnter(resjson, "您当前拥有硬币 " + userMapper.getUser(message.getUser_id()).getCoins() +  "枚");
        addJsonMessageWithEnter(resjson, "输入'交换卡牌 [卡牌名称]'可交换卡牌");
        return resjson;
    }

    /**
     * 交换卡牌
     * @param message
     * @return
     */
    @Override
    public JSONObject doCardExchange(Message message) {
        JSONObject resjson = getResBase(message);
        try {
            String exchangeTarget = message.getCommand().replaceAll("交换卡牌", "").trim();
            if(StringUtils.equals("十连扭蛋券", exchangeTarget)){
                User user = userMapper.getUser(message.getUser_id());

                if(userService.costCoins(user, 20)) {
                    //超过
                    if (user.getLast_get_card() == null || getTodayStartTime() - user.getLast_get_card().getTime() > 0) {
                        user.setCard_left(10);
                    }
                    user.setCard_left(user.getCard_left() + 10);
                    user.setLast_get_card(new Date());
                    userMapper.updateUser(user);
                    addJsonMessageWithEnter(resjson, "交换成功，获得抽卡次数10次，当日有效(剩余抽卡次数" + user.getCard_left() + ")");
                    return resjson;
                } else {
                    addJsonMessageWithEnter(resjson, "交换失败，硬币不足20");
                    return resjson;
                }
            }

            List<Card> shopCards = CardShop.getShopCards();
            int index = 1;
            for (Card c:shopCards){
                if(StringUtils.equals(c.getCard_name(), exchangeTarget)){
                    return exchangeCard(resjson, message, c, 140-20*index);
                }
                index++;
            }
        } catch (Exception e){
            addJsonMessageWithEnter(resjson, "交换失败");
        }
        return resjson;
    }

    /**
     * 卡牌商店随机生成物品
     * @return
     */
    @Override
    public List<Card> createShopCards() {
        int ssrCount = 3;
        int srCount = 3;
        int ssrPrice = 100;
        int srPrice = 50;
        List<Card> cards = cardMapper.getCards();
        List<Card> shopCards = new ArrayList<>();
        List<Card> ssrCards = getCards("SSR", cards);
        List<Card> srCards = getCards("SR", cards);
        for (int i = 0; i < ssrCount; i++) {
            int index = getNextInt(ssrCards.size());
            shopCards.add(ssrCards.get(index));
            ssrCards.remove(index);
        }
        for (int i = 0; i < srCount; i++) {
            int index = getNextInt(srCards.size());
            shopCards.add(srCards.get(index));
            srCards.remove(index);
        }
        return shopCards;
    }

    /**
     * 取出指定稀有度的所有卡牌
      */
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

    /**
     * 抽卡
     * @param cardres  抽卡保存列表
     * @param resjson  抽卡返回信息
     * @param count    抽卡次数
     */
    private void doChouka(List cardres, JSONObject resjson, int count){
        addJsonMessageWithEnter(resjson, "抽卡结果：");
        List<Card> allCards = cardMapper.getCards();
        List<Card> cards;
        Random random = getRandom();
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
                logger.info("[Chouka random2] = " + r2);
                Card getC = cards.get(r2);
                addJsonMessageWithEnter(resjson, (i+1) + "." + getC.getCard_name()+ "(" + getC.getRare() + ")");
                cardres.add(getC);
            } else {
                addJsonMessageWithEnter(resjson, (i+1) + ". 什么都没有抽到");
            }
        }
    }

    /**
     * 储存用户抽到的卡牌信息
     * @param user 用户
     * @param cards 抽到的卡
     * @param message 消息，传递用
     * @param count 抽卡数量
     * @return 获得的硬币数
     */
    private int saveUserCard(User user, List<Card> cards, Message message, int count){
        int userId = user.getUser_id();
        List<Card> userCards = cardMapper.getUserCards(userId);
        int getCoins = 0;
        for(Card c:cards){
            boolean hasCard = false;
            for(Card uc:userCards){
                if(uc.getCard_id() == c.getCard_id()){
                    hasCard = true;
                    switch (c.getRare()){
                        case "N":
                            getCoins += 1;
                            break;
                        case "R":
                            getCoins += 2;
                            break;
                        case "SR":
                            getCoins += 3;
                            break;
                        case "SSR":
                            getCoins += 10;
                            break;
                    }
                    break;
                }
            }
            if(!hasCard){
                c.setUser_id(userId);
                cardMapper.userGetCards(c);
                userCards.add(c);
            }
        }
        user.setCard_left(user.getCard_left() - count);
        user.setLast_get_card(new Date());
        if (getCoins > 0) {
            user.setCoins(user.getCoins() + getCoins);
        }
        checkAchievement(user, message);
        userMapper.updateUser(user);
        return getCoins;
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

    /**
     * 交换卡牌
     * @param resjson
     * @param message
     * @param c
     * @param price
     * @return
     */
    private JSONObject exchangeCard(JSONObject resjson, Message message, Card c, int price) {
        User user = userMapper.getUser(message.getUser_id());
        try {
            if(hasCard(user, c)){
                addJsonMessage(resjson, "交换失败，您已拥有卡牌 " + c.getCard_name());
                return resjson;
            }
            if(price > user.getCoins()){
                addJsonMessage(resjson, "交换失败，硬币不足 " + price);
                return resjson;
            }
            List tmplist = new ArrayList();
            tmplist.add(c);
            user.setCoins(user.getCoins() - price);
            saveUserCard(user, tmplist, message, 0);
            addJsonMessage(resjson, "获得卡牌 " + c.getCard_name());
        } catch (Exception e){
            addJsonMessage(resjson, "您当前无法交换卡牌");
        }
        return resjson;
    }

    // 用户是否拥有卡牌c
    private boolean hasCard(User user, Card c) {
        int userId = user.getUser_id();
        List<Card> userCards = cardMapper.getUserCards(userId);
        for(Card uc:userCards) {
            if (uc.getCard_id() == c.getCard_id()) {
                return true;
            }
        }
        return false;
    }
}
