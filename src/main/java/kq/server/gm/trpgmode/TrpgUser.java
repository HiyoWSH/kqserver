package kq.server.gm.trpgmode;

import kq.server.bean.User;

import java.util.HashMap;
import java.util.Map;

public class TrpgUser {
    /**
     * 用户
     */
    User user;
    /**
     * 用户id
     */
    int userId;
    /**
     * 用户名称
     */
    String name;
    /**
     * 道具列表 [道具名, 数量]
     */
    Map<String, Integer> items = new HashMap<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
