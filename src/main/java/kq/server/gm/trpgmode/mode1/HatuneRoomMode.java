package kq.server.gm.trpgmode.mode1;

import kq.server.bean.User;
import kq.server.gm.trpgmode.Item;
import kq.server.gm.trpgmode.Sence;
import kq.server.gm.trpgmode.TrpgMode;
import kq.server.gm.trpgmode.TrpgUser;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static kq.server.gm.trpgmode.TrpgUtil.*;

public class HatuneRoomMode extends TrpgMode {

    protected Map<String, Mode1Item> items = new HashMap<>();
    private Mode1Item[][] map = new Mode1Item[3][4];
    private static final Logger logger = Logger.getLogger(HatuneRoomMode.class);
    private static String YIZI = "椅";
    private static String ZHUOZI = "桌";
    boolean isEnd;

    @Override
    public void modeLoading() {

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

        map[1][0] = new Mode1Item("窗", "一扇破旧的玻璃窗，玻璃上已经有了一些裂痕", "窗并不像能徒手打开的样子");
        map[1][1] = new Mode1Item(YIZI, "一把有些年代的木质椅子，椅子表面已经布满了裂痕", "你站到了椅子上，并没有什么新发现");
        map[1][2] = new Mode1Item(ZHUOZI, "一只有些年代的木质桌子，十分厚重，桌子表面已经布满了裂痕", "你站到了桌子上，能看的比椅子上更高，不过并没有什么新发现");
        map[2][3] = new Mode1Item("门", "古老的铁门，上面锈迹斑斑，门锁着，看起来不是很容易打开的样子", "门看起来不是很容易打开的样子");

        items.put("窗", map[1][0]);
        items.put(YIZI, map[1][1]);
        items.put(ZHUOZI, map[1][2]);
        items.put("门", map[2][3]);

        sences.put("超时", new Mode1Sence(3*60*1000, "还没有人意识到的时候，似乎一道白光闪过，将一切都吞噬殆尽 Mode end"));

    }

    @Override
    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public void modeEnd() {
        isEnd = true;
    }

    @Override
    public void nextStap() {
        long timeIndex = System.currentTimeMillis() - startTime;
        logger.info("timeIndex" + timeIndex);
        for(TrpgUser user:users.values()){
            if(userOption.containsKey(user.getUserId())){
                List userOp = userOption.get(user.getUserId());
                userOption.put(user.getUserId(), new LinkedList<>());
                if(userOp.size() > 0){
                    String opt = (String) userOp.remove(0);
                    logger.info("执行用户" + user.getUserId() + "命令" + opt);
                    doUserOperate(user, opt);
                }
            }
        }
        for (Sence sc:sences.values()){
            if(sc.getTimeScope() <= timeIndex) {
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
    protected void doUserOperate(TrpgUser trpgUser, String[] opt) {
        switch (opt[0]) {
            case "观察":
                doView(trpgUser, opt[1]);
                break;
            case "操作":
                doOpt(trpgUser, opt);
                break;
            case "移动":
                doMove(trpgUser, opt[1]);
        }
    }


    void doMove(TrpgUser trpgUser, String s) {
        resMsgs.add(items.get(s).move());
    }

    void doView(TrpgUser trpgUser, String s) {
        resMsgs.add(items.get(s).view());
    }

    void doOpt(TrpgUser trpgUser, String[] opt) {
        if(opt.length == 2){
            doOpt(trpgUser, opt[1]);
        } else if(opt.length == 3){
            doOpt(trpgUser, opt[1], opt[2]);
        }
    }

    void doOpt(TrpgUser trpgUser, String s) {
        items.get(s).opt();
        resMsgs.add(trpgUser.getName() + "尝试着去摆弄" + s + "，但是并不能弄出什么名堂来");
    }

    void doOpt(TrpgUser trpgUser, String optFrom, String optTo) {
        if(isDoOpt(optFrom, optTo, YIZI, "门")){
            resMsgs.add(trpgUser.getName() + "尝试着用椅子去撞门");
            int judge = getTrpgJudge();
            resMsgs.add("判定：" + judge + "(?) ... " + judgeMsg(judge, 1));
            if("大成功".equals(judgeMsg(judge, 4))) {
                resMsgs.add("仿佛奇迹一般门被" + trpgUser.getName() + "用椅子撞开，出现了通往外面的路");
                isEnd = true;
                resMsgs.add("EX END");
            } else {
                resMsgs.add("门纹丝不动");
            }
            return;
        }
        if(isDoOpt(optFrom, optTo, YIZI, ZHUOZI)){
            resMsgs.add(trpgUser.getName() + "尝试着把椅子搬到桌子上，并没有什么需要踩上去才能做的事情");
            return;
        }
        if(isDoOpt(optFrom, optTo, YIZI, "窗")){
            resMsgs.add(trpgUser.getName() + "尝试着用椅子去撞窗户");
            int judge = getTrpgJudge();
            String judgeMsg = judgeMsg(judge, 50);
            resMsgs.add("判定：" + judge + "(50) ... " + judgeMsg);
            if(judgeMsg.contains("成功")) {
                resMsgs.add(trpgUser.getName() + "成功用椅子打碎了窗户，现在可以从房间里出去了");
                isEnd = true;
                resMsgs.add("GOOD END");
            } else {
                resMsgs.add("窗户似乎能砸开，不过失败了");
            }
            return;
        }
        if(isDoOpt(optFrom, ZHUOZI)){
            resMsgs.add(trpgUser.getName() + "尝试搬起桌子，并办不到");
            return;
        }
        if(isDoOpt(optFrom, "门")){
            resMsgs.add(trpgUser.getName() + "在门前看了看");
            return;
        }
        if(isDoOpt(optFrom, "窗")){
            resMsgs.add(trpgUser.getName() + "在窗边看了看");
            return;
        }
    }
}
