package kq.server.gm.trpg;

import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TrpgMode {
    List<User> users = new ArrayList<>();
    Map<Long, List<String>> userOption = new HashMap<>();
    long startTime;
    public String description = "";

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Map<Long, List<String>> getUserOption() {
        return userOption;
    }

    public void setUserOption(Map<Long, List<String>> userOption) {
        this.userOption = userOption;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public abstract boolean isEnd();

    public abstract void nextStap();

    public abstract String popResMsg();

    public int getTrpgJudge(){
        int point = RandomUtil.getNextInt(100) + 1;
        return point;
    }

    User getUser(int uid){
        for(User user:users){
            if(uid == user.getUser_id()){
                return user;
            }
        }
        return null;
    }
}
