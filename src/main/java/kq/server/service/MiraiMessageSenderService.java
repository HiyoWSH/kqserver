package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.MiraiMessage;

public interface MiraiMessageSenderService {
    void sendMessage(MiraiMessage message);
    void sendImageWait(String imgUrl);
    String sendMessageGetRes(MiraiMessage message);
    void checkSession();
    String checkSessionOnly();
    void createNewSession();
    void cleanSession();
    void recallMessage(int id);
    void keepAlive();

    String sendImageMessage(JSONObject json);

    void sendMessage(String type, JSONObject json);

    void sendMessage(String type, JSONObject json, long targetId);

    String uploadImage(String imgPath);
}
