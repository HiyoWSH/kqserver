package kq.server.gm.trpg;

import kq.server.bean.User;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class TrpgBaseMode extends TrpgMode {

    Map<String, TrpgItems> items = new HashMap<>();
    List<Sence> sence;
    LinkedList<String> resMsgs = new LinkedList<>();
    boolean isEnd = false;
    // 经过的时间 ms
    long timeIndex = 0;

    public TrpgBaseMode() {
        modeLoaded();
    }

    /**
     * 初始化
     */
    public abstract void modeLoaded();

    abstract void doOpt(User user, String optFrom, String optTo);

    @Override
    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public String popResMsg() {
        if(resMsgs.size() > 0) {
            return resMsgs.pollFirst();
        }
        return "";
    }

    void doUserOperate(User user, String opt) {
        if(StringUtils.isNotBlank(opt)) {
            doUserOperate(user, opt.trim().split(" "));
        }
    }

    /**
     * 执行用户名令 命令的扩展可能要重载
     * @param user
     * @param opt
     */
    void doUserOperate(User user, String[] opt){
        switch (opt[0]) {
            case "观察":
                doView(user, opt[1]);
                break;
            case "操作":
                doOpt(user, opt);
                break;
            case "移动":
                doMove(user, opt[1]);
        }
    }

    void doMove(User user, String s) {
        resMsgs.add(items.get(s).move());
    }

    void doView(User user, String s) {
        resMsgs.add(items.get(s).view());
    }

    void doOpt(User user, String[] opt) {
        if(opt.length == 2){
            doOpt(user, opt[1]);
        } else if(opt.length == 3){
            doOpt(user, opt[1], opt[2]);
        }
    }

    void doOpt(User user, String s) {
        items.get(s).opt();
        resMsgs.add(user.getName() + "尝试着去摆弄" + s + "，但是并不能弄出什么名堂来");
    }

    String judgeMsg(int judge, int i) {
        if(judge <= i && judge <= 4) return "大成功";
        if(judge <= i) return "成功";
        if(judge > i && judge >= 97) return "大失败";
        if(judge > i) return "失败";

        return "失败";
    }

    boolean isDoOpt(String s1, String m1){
        return s1.equals(m1);
    }

    boolean isDoOpt(String s1, String s2, String m1, String m2){
        return (s1.equals(m1) && s2.equals(m2));
    }
}
