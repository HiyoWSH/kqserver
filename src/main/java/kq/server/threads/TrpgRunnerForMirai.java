package kq.server.threads;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.MiraiMessage;
import kq.server.bean.User;
import kq.server.gm.trpgmode.TrpgMode;
import kq.server.gm.trpgmode.TrpgUser;
import kq.server.service.MiraiMessageSenderService;

import java.util.ArrayList;
import java.util.HashMap;

import static kq.server.util.MiraiMessageUtil.*;

public class TrpgRunnerForMirai extends TrpgRunner {
    private MiraiMessage miraiMessage;
    private MiraiMessageSenderService miraiMessageSenderService;

    public TrpgRunnerForMirai(){

    }

    public void setMiraiMessageSenderService(MiraiMessageSenderService miraiMessageSenderService) {
        this.miraiMessageSenderService = miraiMessageSenderService;
    }

    public TrpgRunnerForMirai(MiraiMessage message, TrpgMode trpg, User... users) {
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
        this.miraiMessage = message;
    }

    @Override
    public void sendMsg(String msg){
        JSONObject body = miraiMessage.getBody();
        MiraiMessage message = new MiraiMessage((int) getTarget(body), getType(body), createMessageTextChain(msg));
        miraiMessageSenderService.sendMessage(message);
    }

}
