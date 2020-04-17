package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;

public interface StoryService {
    JSONObject doDealStory(Message message);
    void initStoryCache();
}
