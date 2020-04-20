package kq.server.gm.trpg;

import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.util.RandomUtil;

import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TrpgMode {
    List<User> users = new ArrayList<>();
    Map<Integer, List<String>> userOption = new HashMap<>();
    long startTime;
    public String description = "";

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Map<Integer, List<String>> getUserOption() {
        return userOption;
    }

    public void setUserOption(Map<Integer, List<String>> userOption) {
        this.userOption = userOption;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public abstract boolean end();

    public abstract void nextStap();

    public abstract String popResMsg();

    public int getTrpgJudge(){
        int point = RandomUtil.getNextInt(100) + 1;
        return point;
    }
}
