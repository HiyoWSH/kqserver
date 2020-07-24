package kq.server.threads;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.gm.trpgmode.TrpgUser;
import kq.server.gm.trpgmode.mode1.HatuneRoomMode;
import kq.server.gm.trpgmode.TrpgMode;
import kq.server.util.MessageUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrpgRunner extends Thread {

    private static final Logger logger = Logger.getLogger(TrpgRunner.class);
    protected TrpgMode trpg;
    private Message message;
    private JSONObject resjson;
    protected Map<Long, TrpgUser> userlist;
    protected Map<Long, List<String>> userOption;

    protected TrpgRunner(){}

    public TrpgRunner(Message message, TrpgMode trpg, User... users){
        this.trpg = trpg;
        userlist = new HashMap<>();
        userOption = new HashMap<>();
        for(User u:users){
            TrpgUser tu = new TrpgUser();
            tu.setName(u.getName());
            tu.setUserId(u.getUser_id());
            tu.setUser(u);
            userlist.put(tu.getUserId(), tu);
            userOption.put(u.getUser_id(), new ArrayList());
        }
        this.trpg.setUsers(userlist);
        this.trpg.setUserOption(userOption);
        this.message = message;

    }

    public void setTrpg(TrpgMode trpg){
        this.trpg = trpg;
    }

    @Override
    public void run(){
        long startTime = System.currentTimeMillis();

        trpg.modeLoading();
        trpg.setStartTime(startTime);

//        resjson = MessageUtil.addJsonMessage(MessageUtil.getResBase(message), trpg.getDescription());
//        message.setResbody(resjson);
//        MessageSender.sendMessage(message);
        sendMsg(trpg.getDescription());

        while (!trpg.isEnd()){
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
                    sendMsg(res);
                    Thread.sleep(1000);
                    hasRes = true;
                }
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
    }

    protected void sendMsg(String res) {
        if(message != null) {
            resjson = MessageUtil.addJsonMessage(MessageUtil.getResBaseNoAt(message), res);
            message.setResbody(resjson);
            MessageSender.sendMessage(message);
        }
    }

    public void addUser(User user) {
        TrpgUser tu = new TrpgUser();
        tu.setName(user.getName());
        tu.setUserId(user.getUser_id());
        userlist.put(tu.getUserId(), tu);
        userOption.put(user.getUser_id(), new ArrayList());
    }

    public List<User> getUserlist() {
        List<User> users = new ArrayList<>();
        for(TrpgUser tuser:userlist.values()){
            users.add(tuser.getUser());
        }
        return users;
    }

    public Map<Long, List<String>> getUserOption() {
        return userOption;
    }
}
