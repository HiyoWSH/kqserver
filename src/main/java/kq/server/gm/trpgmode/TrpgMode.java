package kq.server.gm.trpgmode;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class TrpgMode {
    /**
     * 模组用户列表 [用户id，用户]
     */
    protected Map<Long, TrpgUser> users = new HashMap<>();
    /**
     * 模组物品列表 [物品名，物品]
     */
    protected Map<String, Item> items = new HashMap<>();
    /**
     * 模组场景列表 [场景名，场景]
     */
    protected Map<String, Sence> sences = new HashMap<>();
    /**
     * 用户指令列表 [用户id，用户指令列表]
     */
    protected Map<Long, List<String>> userOption = new HashMap<>();
    /**
     * 返回消息列表
     */
    protected LinkedList<String> resMsgs = new LinkedList<>();
    /**
     * 模组开始时间戳
     */
    protected long startTime = System.currentTimeMillis();
    /**
     * 模组描述
     */
    protected String description = "模组描述";

    /**
     * 载入模组
     */
    public abstract void modeLoading();
    /**
     * 模组是否结束
     * @return
     */
    public abstract boolean isEnd();
    public abstract void modeEnd();
    /**
     * 模组下一次行动
     */
    public abstract void nextStap();
    /**
     * 弹出返回消息
     * @return
     */
    public String popResMsg(){
        if(resMsgs.size() > 0) {
            String scRes = resMsgs.pollFirst();
            if(scRes.contains("BAD END") || scRes.contains("HAPPY END")){
                modeEnd();
            }
            return scRes;
        }
        return "";
    }

    /**
     * 解析用户命令
     * @param trpgUser
     * @param opt
     */
    protected void doUserOperate(TrpgUser trpgUser, String opt) {
        if(StringUtils.isNotBlank(opt)) {
            doUserOperate(trpgUser, opt.trim().split(" "));
        }
    }

    /**
     * 执行用户命令
     * @param trpgUser
     * @param opts
     */
    protected abstract void doUserOperate(TrpgUser trpgUser, String[] opts);

    // getter and setter

    public Map<Long, TrpgUser> getUsers() {
        return users;
    }

    public void setUsers(Map<Long, TrpgUser> users) {
        this.users = users;
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public void setItems(Map<String, Item> items) {
        this.items = items;
    }

    public Map<String, Sence> getSences() {
        return sences;
    }

    public void setSences(Map<String, Sence> sences) {
        this.sences = sences;
    }

    public Map<Long, List<String>> getUserOption() {
        return userOption;
    }

    public void setUserOption(Map<Long, List<String>> userOption) {
        this.userOption = userOption;
    }

    public LinkedList<String> getResMsgs() {
        return resMsgs;
    }

    public void setResMsgs(LinkedList<String> resMsgs) {
        this.resMsgs = resMsgs;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
