package kq.server.service;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;

public interface MessageHandlerService {
    /**
     * 消息处理函数
     * @param message
     */
    void doDeal(Message message);
}
