package kq.server.service;

import kq.server.bean.User;

public interface UserService {
    boolean costCoins(User user, int count);
    boolean costCoins(int user_id, int count);

    boolean getCoins(User user, int count);
    boolean getCoins(int user_id, int count);
    String showUserInfo(User user);
}
