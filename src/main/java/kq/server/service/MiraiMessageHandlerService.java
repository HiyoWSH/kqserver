package kq.server.service;

import com.alibaba.fastjson.JSONObject;

public interface MiraiMessageHandlerService {
    String doDeal(JSONObject body);
    void sendImage(JSONObject targetJson, String imageUrl);
}
