package kq.server.bean;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

public class User {
    String name;
    long user_id;
    int card_left = 10;
    Date last_get_card;
    Date last_get_qian;
    List<Card> cards;
    String last_qian;
    String achievements;
    String status;
    int coins;

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public int getCard_left() {
        return card_left;
    }

    public void setCard_left(int card_left) {
        this.card_left = card_left;
    }

    public Date getLast_get_card() {
        return last_get_card;
    }

    public void setLast_get_card(Date last_get_card) {
        this.last_get_card = last_get_card;
    }

    public Date getLast_get_qian() {
        return last_get_qian;
    }

    public void setLast_get_qian(Date last_get_qian) {
        this.last_get_qian = last_get_qian;
    }

    public String getLast_qian() {
        return last_qian;
    }

    public void setLast_qian(String last_qian) {
        this.last_qian = last_qian;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public void addAchievement(Achievement achievement) {
        if(StringUtils.isEmpty(achievements)){
            achievements = "";
        }
        achievements = achievements + "," + achievement.getAchievement_name();
    }

    public boolean hasAchievement(Achievement achievement) {
        return achievements != null && achievements.contains(achievement.getAchievement_name());
    }

    public String[] getAchievementsArray() {
        if(StringUtils.isBlank(achievements)){
            return new String[]{};
        }
        return achievements.split(",");
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
