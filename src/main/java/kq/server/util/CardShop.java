package kq.server.util;

import kq.server.bean.Card;

import java.util.ArrayList;
import java.util.List;

public class CardShop {
    private static List<Card> shopCards = new ArrayList<>();

    public static List<Card> getShopCards() {
        return shopCards;
    }

    public static void setShopCards(List<Card> shopCards) {
        CardShop.shopCards = shopCards;
    }
}
