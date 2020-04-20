package kq.server.threads;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.gm.trpg.HatuneRoomMode;
import kq.server.gm.trpg.TrpgMode;
import kq.server.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrpgRunner extends Thread {

    private static final Logger logger = Logger.getLogger(TrpgRunner.class);
    private TrpgMode trpg;
    private Message message;
    private JSONObject resjson;
    private List<User> userlist;
    Map<Integer, List<String>> userOption;

    public TrpgRunner(Message message, User... users){
        trpg = new HatuneRoomMode();
        userlist = new ArrayList<>();
        userOption = new HashMap<>();
        for(User u:users){
            userlist.add(u);
            userOption.put(u.getUser_id(), new ArrayList());
        }
        trpg.setUsers(userlist);
        trpg.setUserOption(userOption);
        this.message = message;

    }

    @Override
    public void run(){
        long startTime = System.currentTimeMillis();

        trpg.setStartTime(startTime);

        resjson = MessageUtil.addJsonMessage(MessageUtil.getResBase(message), trpg.description);
        message.setResbody(resjson);
        MessageSender.sendMessage(message);

        while (!trpg.end()){
            try {
                boolean hasRes = false;
                trpg.nextStap();
                Thread.sleep(5000);
                String res = "";
                while (true){
                    res = trpg.popResMsg();
                    if(StringUtils.isBlank(res)){
                        break;
                    }
                    resjson = MessageUtil.addJsonMessage(MessageUtil.getResBaseNoAt(message), res);
                    message.setResbody(resjson);
                    MessageSender.sendMessage(message);
                    Thread.sleep(1000);
                    hasRes = true;
                }

//                if(!hasRes) {
//                    resjson = MessageUtil.addJsonMessage(MessageUtil.getResBase(message), "什么都没有发生");
//                    message.setResbody(resjson);
//                    MessageSender.sendMessage(message);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addUser(User user) {
        userlist.add(user);
        userOption.put(user.getUser_id(), new ArrayList());
    }

    public List<User> getUserlist() {
        return userlist;
    }

    public Map<Integer, List<String>> getUserOption() {
        return userOption;
    }
}
