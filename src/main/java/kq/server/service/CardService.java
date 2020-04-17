package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Card;
import kq.server.bean.Message;

import java.util.List;

public interface CardService {
    JSONObject doGetCard(Message message, int count);
    JSONObject doShowCard(Message message);
    JSONObject doDealChouka(Message message);
    JSONObject doShowCardShop(Message message);
    JSONObject doCardExchange(Message message);
    List<Card> createShopCards();
}
