package kq.server.gm.trpg;

import kq.server.bean.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class HatuneRoomMode extends TrpgMode {

    private TrpgItems[][] map = new TrpgItems[3][4];
    private Map<String, TrpgItems> items = new HashMap<>();
    private List<Sence> sence;
    private LinkedList<String> resMsgs = new LinkedList<>();
    boolean isEnd = false;
    private static final Logger logger = Logger.getLogger(HatuneRoomMode.class);
    private static String YIZI = "椅";
    private static String ZHUOZI = "桌";

    // 经过的时间 ms
    private long timeIndex = 0;

    public HatuneRoomMode() {
        this.description = "模组MODE1\n" +
                "目标：逃离房间\n" +
                "失败条件：？\n" +
                "指令列表：\n" +
                "观察 [对象1]          （观察 门    观察门）\n" +
                "操作 [对象1]          （操作 门    操作门）\n" +
                "操作 [对象1] [对象2]  （操作 门 窗 使用门操作窗）\n" +
                "移动 [对象1]           （移动 桌子  移动到桌子的位置）\n" +
                "\n" +
                "\n" +
                "当你醒来的时候，你发现自己正站在一件3*4大小的房间之中\n" +
                "一一门\n" +
                "一桌一\n" +
                "一椅一\n" +
                "窗一一\n";

        sence = new ArrayList();
        map[1][0] = new TrpgItems("窗", "一扇破旧的玻璃窗，玻璃上已经有了一些裂痕", "窗并不像能徒手打开的样子");
        map[1][1] = new TrpgItems(YIZI, "一把有些年代的木质椅子，椅子表面已经布满了裂痕", "你站到了椅子上，并没有什么新发现");
        map[1][2] = new TrpgItems(ZHUOZI, "一只有些年代的木质桌子，十分厚重，桌子表面已经布满了裂痕", "你站到了桌子上，能看的比椅子上更高，不过并没有什么新发现");
        map[2][3] = new TrpgItems("门", "古老的铁门，上面锈迹斑斑，门锁着，看起来不是很容易打开的样子", "门看起来不是很容易打开的样子");

        items.put("窗", map[1][0]);
        items.put(YIZI, map[1][1]);
        items.put(ZHUOZI, map[1][2]);
        items.put("门", map[2][3]);

        sence.add(new Sence(3*60*1000, "还没有人意识到的时候，似乎一道白光闪过，将一切都吞噬殆尽 Mode end"));

    }

    @Override
    public boolean end() {
        return isEnd;
    }

    @Override
    public void nextStap() {
        timeIndex = System.currentTimeMillis() - startTime;
        logger.info("timeIndex" + timeIndex);
        for(User user:users){
            if(userOption.containsKey(user.getUser_id())){
                List userOp = userOption.get(user.getUser_id());
                userOption.put(user.getUser_id(), new LinkedList<>());
                if(userOp.size() > 0){
                    String opt = (String) userOp.remove(0);
                    logger.info("执行用户" + user.getUser_id() + "命令" + opt);
                    doUserOperate(user, opt);
                }
            }
        }
        for (Sence sc:sence){
            if(sc.getTime() <= timeIndex) {
                String scRes = sc.event();
                resMsgs.add(scRes);
                if(scRes.contains("Mode end")){
                    isEnd = true;
                    resMsgs.add("BAD END");
                }
            }
        }
    }

    @Override
    public String popResMsg() {
        if(resMsgs.size() > 0) {
            return resMsgs.pollFirst();
        }
        return "";
    }

    private void doUserOperate(User user, String opt) {
        if(StringUtils.isNotBlank(opt)) {
            doUserOperate(user, opt.trim().split(" "));
        }
    }

    private void doUserOperate(User user, String[] opt){
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

    private void doMove(User user, String s) {
        resMsgs.add(items.get(s).move());
    }

    private void doView(User user, String s) {
        resMsgs.add(items.get(s).view());
    }

    private void doOpt(User user, String[] opt) {
        if(opt.length == 2){
            doOpt(user, opt[1]);
        } else if(opt.length == 3){
            doOpt(user, opt[1], opt[2]);
        }
    }

    private void doOpt(User user, String s) {
        items.get(s).opt();
        resMsgs.add(user.getName() + "尝试着去摆弄" + s + "，但是并不能弄出什么名堂来");
    }

    private void doOpt(User user, String optFrom, String optTo) {
        if(isDoOpt(optFrom, optTo, YIZI, "门")){
            resMsgs.add(user.getName() + "尝试着用椅子去撞门");
            int judge = getTrpgJudge();
            resMsgs.add("判定：" + judge + "(?) ... " + judgeMsg(judge, 1));
            if("大成功".equals(judgeMsg(judge, 4))) {
                resMsgs.add("仿佛奇迹一般门被" + user.getName() + "用椅子撞开，出现了通往外面的路");
                isEnd = true;
                resMsgs.add("EX END");
            } else {
                resMsgs.add("门纹丝不动");
            }
            return;
        }
        if(isDoOpt(optFrom, optTo, YIZI, ZHUOZI)){
            resMsgs.add(user.getName() + "尝试着把椅子搬到桌子上，并没有什么需要踩上去才能做的事情");
            return;
        }
        if(isDoOpt(optFrom, optTo, YIZI, "窗")){
            resMsgs.add(user.getName() + "尝试着用椅子去撞窗户");
            int judge = getTrpgJudge();
            String judgeMsg = judgeMsg(judge, 50);
            resMsgs.add("判定：" + judge + "(50) ... " + judgeMsg);
            if(judgeMsg.contains("成功")) {
                resMsgs.add(user.getName() + "成功用椅子打碎了窗户，现在可以从房间里出去了");
                isEnd = true;
                resMsgs.add("GOOD END");
            } else {
                resMsgs.add("窗户似乎能砸开，不过失败了");
            }
            return;
        }
        if(isDoOpt(optFrom, ZHUOZI)){
            resMsgs.add(user.getName() + "尝试搬起桌子，并办不到");
            return;
        }
        if(isDoOpt(optFrom, "门")){
            resMsgs.add(user.getName() + "在门前看了看");
            return;
        }
        if(isDoOpt(optFrom, "窗")){
            resMsgs.add(user.getName() + "在窗边看了看");
            return;
        }
    }

    private String judgeMsg(int judge, int i) {
        if(judge <= i && judge <= 4) return "大成功";
        if(judge <= i) return "成功";
        if(judge > i && judge >= 97) return "大失败";
        if(judge > i) return "失败";

        return "失败";
    }

    private boolean isDoOpt(String s1, String m1){
        return s1.equals(m1);
    }

    private boolean isDoOpt(String s1, String s2, String m1, String m2){
        return (s1.equals(m1) && s2.equals(m2));
    }
}
