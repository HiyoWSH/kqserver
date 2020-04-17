package kq.server.service;

import kq.server.bean.Message;

public interface MessageHandlerService {
    /**
     * 消息处理函数
     * @param message
     * @param command
     */
    void doDeal(Message message);
}
