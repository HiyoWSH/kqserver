package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;

public interface StoryService {
    String doDealStory(String command);
    void initStoryCache();
}
