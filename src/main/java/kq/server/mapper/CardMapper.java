package kq.server.mapper;

import kq.server.bean.Card;

import java.util.List;

public interface CardMapper {
    public List<Card> getUserCards(long user_id);
    public List<Card> getCards();
    public void userGetCards(Card card);
    void insertCard(Card card);
}
