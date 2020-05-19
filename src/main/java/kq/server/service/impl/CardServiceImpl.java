package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Card;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.CardMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.CardService;
import kq.server.service.MiraiMessageSenderService;
import kq.server.service.UserService;
import kq.server.threads.AchievementSender;
import kq.server.threads.MiraiSender;
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
    @Autowired
    MiraiMessageSenderService miraiMessageSenderService;

    /**
     * 抽卡 count 次
     * @param count
     * @return
     */
    @Override
    public String doGetCard(User user, int count) {
        StringBuilder stringBuilderRes = new StringBuilder();
        //超过
        if(user.getLast_get_card() == null || getTodayStartTime() - user.getLast_get_card().getTime() > 0){
            user.setCard_left(10);
        }
        if(user.getCard_left() < count){
            return String.format("今天的剩余抽卡次数是%d次哦，请不要太贪心", user.getCard_left());
        }

        List<Card> getResCard = new ArrayList<>();
        stringBuilderRes.append(doChouka(getResCard, count));
        int getCoins = saveUserCard(user, getResCard, count);
        if(getCoins > 0){
            stringBuilderRes.append("\n").append(String.format("抽到的重复卡牌转换为硬币 %d枚", getCoins));
            stringBuilderRes.append("\n").append(String.format("当前拥有硬币 %d枚", userMapper.getUser(user.getUser_id()).getCoins()));
        }
        stringBuilderRes.append("\n").append("输入“查看卡牌”查看已抽取到的卡牌");
        return stringBuilderRes.toString();
    }

    /**
     * 查看卡牌
     * @return
     */
    @Override
    public String doShowCard(User user, String command){
        StringBuilder stringBuilderRes = new StringBuilder();
        try{
            String cardName = command.split("查看卡.")[1].trim();
            if(!"".equals(cardName)){
                List<Card> cards = cardMapper.getCards();
                for(Card c:cards){
                    if(cardName.equals(c.getCard_name())){
                        stringBuilderRes.append("\n").append("卡牌名称：" + cardName);
                        stringBuilderRes.append("\n").append("稀有度：" + c.getRare());
                        stringBuilderRes.append("\n").append(JSONObject.parseObject(c.getCard_description()).getString("description"));
                        if(c.getImagepath() != null){
                            miraiMessageSenderService.sendImageWait(c.getImagepath());
                        }
                        return stringBuilderRes.toString();
                    }
                }
                stringBuilderRes.append("\n").append("未找到卡牌 " + cardName);
                return stringBuilderRes.toString();
            }
        }catch (Exception e){
            System.out.println("未输入卡牌名称");
        }

        int user_id = user.getUser_id();
        List<Card> userCards = cardMapper.getUserCards(user_id);
        if(userCards.size() == 0){
            stringBuilderRes.append("\n").append("你还没有获得任何卡牌呢，输入抽卡抽取");
            return stringBuilderRes.toString();
        }
        stringBuilderRes.append("\n").append("你拥有的卡牌如下：");
        for(Card c:userCards){
            stringBuilderRes.append("\n").append(String.format("%s(%s)", c.getCard_name(), c.getRare()));
        }
        stringBuilderRes.append("\n").append("输入查看卡牌 [卡牌名称] 查看卡牌详细信息");
        return stringBuilderRes.toString();
    }

    /**
     * 抽卡命令入口
     * @return
     */
    @Override
    public String doDealChouka(User user, String command){
        int count = 1;
        try{
            System.out.println("command " + command);
            count = Integer.parseInt(command.replaceAll("[^0-9]",""));
        } catch (Exception e){e.printStackTrace();}
        return doGetCard(user, count);
    }

    /**
     * 卡牌商店
     * @return
     */
    @Override
    public String doShowCardShop(User user) {
        StringBuilder stringBuilderRes = new StringBuilder();
        stringBuilderRes.append("\n").append("今日可交换卡牌");
        List<Card> shopCards = CardShop.getShopCards();
        int index = 1;
        for (Card c:shopCards){

            stringBuilderRes.append("\n").append(String.format("%d.%s (%s)......%d硬币", index, c.getCard_name(), c.getRare(), (140-20*index)));
            index++;
        }
        stringBuilderRes.append("\n").append(index + ".十连扭蛋券......20硬币");
        stringBuilderRes.append("\n");
        stringBuilderRes.append("\n").append("您当前拥有硬币 " + userMapper.getUser(user.getUser_id()).getCoins() +  "枚");
        stringBuilderRes.append("\n").append("输入'交换卡牌 [卡牌名称]'可交换卡牌");
        return stringBuilderRes.toString();
    }

    /**
     * 交换卡牌
     * @return
     */
    @Override
    public String doCardExchange(User user, String command) {
        StringBuilder stringBuilderRes = new StringBuilder();
        try {
            String exchangeTarget = command.replaceAll("交换卡牌", "").trim();
            if(StringUtils.equals("十连扭蛋券", exchangeTarget)){
                if(userService.costCoins(user, 20)) {
                    //超过
                    if (user.getLast_get_card() == null || getTodayStartTime() - user.getLast_get_card().getTime() > 0) {
                        user.setCard_left(10);
                    }
                    user.setCard_left(user.getCard_left() + 10);
                    user.setLast_get_card(new Date());
                    userMapper.updateUser(user);
                    stringBuilderRes.append("\n").append("交换成功，获得抽卡次数10次，当日有效(剩余抽卡次数" + user.getCard_left() + ")");
                    return stringBuilderRes.toString();
                } else {
                    stringBuilderRes.append("\n").append("交换失败，硬币不足20");
                    return stringBuilderRes.toString();
                }
            }

            List<Card> shopCards = CardShop.getShopCards();
            int index = 1;
            for (Card c:shopCards){
                if(StringUtils.equals(c.getCard_name(), exchangeTarget)){
                    return exchangeCard(user, c, 140-20*index);
                }
                index++;
            }
        } catch (Exception e){
            stringBuilderRes.append("\n").append("交换失败");
        }
        return stringBuilderRes.toString();
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
     * @return  resjson  抽卡返回信息
     * @param count    抽卡次数
     */
    private String doChouka(List cardres, int count){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("抽卡结果：");
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
                stringBuilder.append("\n").append(String.format("%d.%s(%s)", i+1, getC.getCard_name(), getC.getRare()));
                cardres.add(getC);
            } else {
                stringBuilder.append("\n").append(String.format("%d. 什么都没有抽到", i+1));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 储存用户抽到的卡牌信息
     * @param user 用户
     * @param cards 抽到的卡
     * @param count 抽卡数量
     * @return 获得的硬币数
     */
    private int saveUserCard(User user, List<Card> cards, int count){
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
        userMapper.updateUser(user);
        return getCoins;
    }

    /**
     * 检查是否满足新的成就
     * @param user
     */
    @Override
    public void checkAchievement(User user, Message message) {
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
                    if(achievement.getImagepath() != null) {
                        miraiMessageSenderService.sendImageWait(achievement.getImagepath());
                    }
                }
            }
        }
        userMapper.updateUser(user);
    }

    /**
     * 交换卡牌
     * @param c
     * @param price
     * @return
     */
    private String exchangeCard(User user, Card c, int price) {
        StringBuilder stringBuilderRes = new StringBuilder();
        try {
            if(hasCard(user, c)){
                return stringBuilderRes.append("交换失败，您已拥有卡牌 " + c.getCard_name()).toString();
            }
            if(price > user.getCoins()){
                return stringBuilderRes.append("交换失败，硬币不足 " + price).toString();
            }
            List tmplist = new ArrayList();
            tmplist.add(c);
            user.setCoins(user.getCoins() - price);
            saveUserCard(user, tmplist, 0);
            stringBuilderRes.append("获得卡牌 " + c.getCard_name()).toString();
        } catch (Exception e){
            stringBuilderRes.append("您当前无法交换卡牌").toString();
        }
        return stringBuilderRes.toString();
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
