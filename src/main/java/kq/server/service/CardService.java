package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Card;
import kq.server.bean.Message;
import kq.server.bean.User;

import java.util.List;

public interface CardService {
    String doGetCard(User user, int count);
    String doShowCard(User user, String command);
    String doDealChouka(User user, String command);
    String doShowCardShop(User user);
    String doCardExchange(User user, String command);
    List<Card> createShopCards();
    void checkAchievement(User user, Message message);
    void checkAchievement(User user, JSONObject body);
}
