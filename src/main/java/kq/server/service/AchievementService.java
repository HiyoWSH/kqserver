package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;

public interface AchievementService {
    JSONObject doShowAchievement(Message message);
}
