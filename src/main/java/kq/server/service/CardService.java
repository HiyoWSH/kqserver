package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;

public interface CardService {
    JSONObject doGetCard(Message message, int count);
    JSONObject doShowCard(Message message);
}
