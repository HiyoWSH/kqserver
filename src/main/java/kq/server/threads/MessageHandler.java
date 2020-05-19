package kq.server.threads;

import kq.server.bean.Message;
import kq.server.service.MessageHandlerService;
import org.apache.log4j.Logger;

import java.util.LinkedList;

public class MessageHandler extends Thread {

    private static Logger logger = Logger.getLogger(MessageHandler.class);
    // 正在处理的消息
    private static Message doingMessage;

    MessageHandlerService messageHandlerService;
    static LinkedList<Message> messageList = new LinkedList<Message>();

    public MessageHandler(MessageHandlerService messageHandlerService) {
        this.messageHandlerService = messageHandlerService;
    }

    public static void dealMessage(Message message){
        messageList.add(message);
    }

    public static Message getDoingMessage(){
        return doingMessage;
    }

    @Override
    public void run(){
        while (true){
            try {
                if (messageList.size() > 0) {
                    Message message = messageList.pop();
                    if (message.needDeal()) {
                        doingMessage = message;
                        messageHandlerService.doDeal(message);
                        doingMessage = null;
                    }
                }

                Thread.sleep(200);
            } catch (Exception e){
                e.printStackTrace();
                logger.error(e);
            }
        }
    }

}
