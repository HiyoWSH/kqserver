package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;

public interface AchievementService {
    String doShowAchievement(User user, String command);
}
