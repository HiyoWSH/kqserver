package kq.server.gm.trpgmode.mode2;

import kq.server.gm.trpg.TrpgItems;
import kq.server.gm.trpgmode.Item;
import kq.server.gm.trpgmode.Sence;
import kq.server.gm.trpgmode.TrpgMode;
import kq.server.gm.trpgmode.TrpgUser;
import kq.server.gm.trpgmode.mode2.sence.KinokSence;
import org.apache.log4j.Logger;

import java.util.*;
import static kq.server.gm.trpgmode.TrpgUtil.*;

public class ZonbiRoomMode extends TrpgMode {

    private static final Logger logger = Logger.getLogger(kq.server.gm.trpg.ZonbiRoomMode.class);
    private static final String TABLE1 = "桌1";
    private static final String TABLE2 = "桌2";
    private static final String DOOR1 = "门1";
    private static final String DOOR2 = "门2";
    private static final String CHAIR1 = "椅1";
    private static final String CHAIR2 = "椅2";
    private static final String WINDOW1 = "窗1";
    private static final String WINDOW2 = "窗2";

    private static final String 钥匙 = "钥匙";
    private static final String 剪刀 = "剪刀";
    boolean isEnd = false;

    boolean table1InDoor1 = false;
    boolean table2InDoor1 = false;
    boolean chair1InDoor1 = false;
    boolean chair2InDoor1 = false;

    boolean table1InDoor2 = false;
    boolean table2InDoor2 = false;
    boolean chair1InDoor2 = false;
    boolean chair2InDoor2 = false;

    boolean chair1In = true;
    boolean chair2In = true;
    boolean table1In = true;
    boolean table2In = true;

    List<TrpgUser> table2Move = new ArrayList<>();

    @Override
    public void modeLoading() {
        this.description = "模组MODE2\n" +
                "目标：？\n" +
                "失败条件：？\n" +
                "指令列表：\n" +
                "观察 [对象1]          例：（观察 门1）\n" +
                "操作 [对象1]          例：（操作 门1) \n" +
                "操作 [对象1] [对象2]   例：（操作 门1 窗1 使用门操作窗）\n" +
                "移动 [坐标]           例：（移动 桌1 移动到桌1的位置）\n" +
                "背包                  （查看拥有的道具，未实现）\n" +
                "\n" +
                "当你意识到的时候已经在这样一间房间之中，直觉告诉你这里并不安全。\n" +
                "地图：\n" +
                "门1一一一一一一门2\n" +
                "一一一一一一一一一\n" +
                "一一桌1一一桌2一一\n" +
                "一一椅1一一椅2一一\n" +
                "一一一一一一一一一\n" +
                "一一窗1一一窗2一一";

        Item t1 = new Item(TABLE1,"一张学生桌","站到了桌1上，能看的比椅子上更高，不过并没有什么新发现");
        Item t2 = new Item(TABLE2,"一只较为厚重的办公桌","站到了桌2上，能看的比学生桌上更高，不过并没有什么新发现");
        Item c1 = new Item(CHAIR1,"一张学生椅，应该可以很轻松的搬动","站到了椅1上，并没有什么新发现");
        Item c2 = new Item(CHAIR2,"一只看起来坐上去会很舒适的办公椅，不过现在坐上去会染上一身灰吧","坐在了椅2上，染了一身灰");
        Item d1 = new Item(DOOR1,"木质拉门，可以从内部上锁","走到了门1前，只是一扇普通的木门");
        Item d2 = new Item(DOOR2,"铁质推门，被锁住了无法打开","走到了门2前，只是一扇普通的铁门，应该比木门坚固一些");
        Item w1 = new Item(WINDOW1,"普通的外翻窗，看起来已经有一段时间没用被使用过了，堆积着不少灰尘","到窗1口看了下，这里似乎是4楼，从窗户没法下去吧");
        Item w2 = new Item(WINDOW2,"普通的外翻窗，看起来已经有一段时间没用被使用过了，堆积着不少灰尘","到窗2口看了下，这里似乎是4楼，从窗户没法下去吧");
        Item jd = new Item(剪刀);
        Item kaki = new Item(钥匙);

        items.put(TABLE1, t1);
        items.put(TABLE2, t2);
        items.put(DOOR1, d1);
        items.put(DOOR2, d2);
        items.put(CHAIR1, c1);
        items.put(CHAIR2, c2);
        items.put(WINDOW1, w1);
        items.put(WINDOW2, w2);
        items.put(钥匙, kaki);
        items.put(剪刀, jd);

        sences.put("voice1", new Sence(30*6*1000, "门外似乎传来了一声低沉的“eee~”声", this));
        sences.put("voice2", new Sence(35*6*1000, "门外再次传来了低沉的“eee~”声，伴随着的是什么东西在地上被拖动的声音", this));
        sences.put("voice3", new Sence(40*6*1000, "门外再次传来了低沉的“eee~”声，伴随着的是什么东西在地上被拖动的声音", this));
        sences.put("voice4", new Sence(45*6*1000, "门外再次传来了低沉的“eee~”声，伴随着的是什么东西在地上被拖动的声音，声音似乎正在接近", this));
        sences.put("kinck1", new KinokSence(50*6*1000, this));

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
                    try {
                        String opt = (String) userOp.remove(0);
                        logger.info("执行用户" + user.getUserId() + "命令" + opt);
                        doUserOperate(user, opt);
                    } catch (Exception e){
                        logger.error(e);
                    }
                }
            }
        }
        for (String ksc:sences.keySet()){
            Sence sc = sences.get(ksc);
            if(sc.getTimeScope() <= timeIndex) {
                String scRes = sc.event();
                resMsgs.add(scRes);
                sc.setTimeScope(Long.MAX_VALUE);
            }
        }
    }

    @Override
    protected void doUserOperate(TrpgUser trpgUser, String[] opts) {
        switch (opts[0]) {
            case "观察":
                doView(trpgUser, opts[1]);
                break;
            case "操作":
                doOpt(trpgUser, opts);
                break;
            case "移动":
                doMove(trpgUser, opts[1]);
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

    void doOpt(TrpgUser user, String optFrom){
        if(isDoOpt(optFrom, DOOR1) || isDoOpt(optFrom, DOOR2)){
            resMsgs.add(String.format("%s打开了%s，出现在他眼前的是一群xx... 所有人都被撕成了碎片", user.getName(), optFrom));
            isEnd = true;
            resMsgs.add("BAD END - 001 门外的xx");
            return;
        }

        if(isDoOpt(optFrom, WINDOW1) || isDoOpt(optFrom, WINDOW1)){
            resMsgs.add(String.format("%s打开了%s，新鲜的空气让你紧绷的神经轻松了一些", user.getName(), optFrom));
            return;
        }
        resMsgs.add(this.items.get(optFrom).getViewMse());
    }

    void doOpt(TrpgUser user, String optFrom, String optTo) {
        if(isDoOpt(CHAIR1, optFrom) && !chair1In){
            resMsgs.add(String.format("%s已经不在这个房间里了", optFrom));
            return;
        }
        if(isDoOpt(CHAIR2, optFrom) && !chair2In){
            resMsgs.add(String.format("%s已经不在这个房间里了", optFrom));
            return;
        }
        if(isDoOpt(TABLE1, optFrom) && !table1In){
            resMsgs.add(String.format("%s已经不在这个房间里了", optFrom));
            return;
        }
        if(isDoOpt(TABLE2, optFrom) && !table2In){
            resMsgs.add(String.format("%s已经不在这个房间里了", optFrom));
            return;
        }

        if(isDoOpt(CHAIR1, DOOR1, optFrom, optTo)){
            if(!chair1InDoor1){
                chair1InDoor1 = true;
                chair1InDoor2 = false;
                resMsgs.add(String.format("%s把%s搬到了%s处", user.getName(), optFrom, optTo));
            } else {
                resMsgs.add(String.format("%s已经在%s处了", optFrom, optTo));
            }
            return;
        }
        if(isDoOpt(CHAIR2, DOOR1, optFrom, optTo)){
            if(!chair2InDoor1){
                chair2InDoor1 = true;
                chair2InDoor2 = false;
                resMsgs.add(String.format("%s把%s搬到了%s处", user.getName(), optFrom, optTo));
            } else {
                resMsgs.add(String.format("%s已经在%s处了", optFrom, optTo));
            }
            return;
        }
        if(isDoOpt(CHAIR1,WINDOW1, optFrom, optTo) || isDoOpt(CHAIR1,WINDOW2, optFrom, optTo)){
            chair1In = false;
            resMsgs.add(String.format("%s把%s扔到了窗外", user.getName(), optFrom));
        }
        if(isDoOpt(CHAIR2,WINDOW1, optFrom, optTo) || isDoOpt(CHAIR2,WINDOW2, optFrom, optTo)){
            chair2In = false;
            resMsgs.add(String.format("%s把%s扔到了窗外", user.getName(), optFrom));
        }
        if(isDoOpt(TABLE1,WINDOW1, optFrom, optTo) || isDoOpt(TABLE1,WINDOW2, optFrom, optTo)){
            table1In = false;
            resMsgs.add(String.format("%s把%s扔到了窗外", user.getName(), optFrom));
        }
        if(isDoOpt(TABLE1, DOOR1, optFrom, optTo)){
            if(!table1InDoor1){
                table1InDoor1 = true;
                table1InDoor2 = false;
                resMsgs.add(String.format("%s把%s搬到了%s处", user.getName(), optFrom, optTo));
            } else {
                resMsgs.add(String.format("%s已经在%s处了", optFrom, optTo));
            }
            return;
        }

        if(isDoOpt(TABLE2, optFrom)){
            if(table2Move.size() == 1 && table2Move.get(0).getUserId() != user.getUserId()) {
                table2Move.add(user);
            }
            if(table2Move.size() > 1){
                resMsgs.add(String.format("在%s和%s的合力之下终于成功的吧%s移动到了%s处", table2Move.get(0).getName(), table2Move.get(1).getName(), optFrom, optTo));
                if(isDoOpt(DOOR1, optTo)){
                    table2InDoor1 = true;
                    table2InDoor2 = false;
                } else if(isDoOpt(DOOR2, optTo)){
                    table2InDoor1 = false;
                    table2InDoor2 = true;
                } else if(isDoOpt(WINDOW1, optTo) || isDoOpt(WINDOW2, optTo)){
                    table2In = false;
                    resMsgs.add("桌2被扔出了窗外");
                } else {
                    table2InDoor1 = false;
                    table2InDoor2 = false;
                }
            } else {
                resMsgs.add(String.format("%s尝试着搬动%s，可是太重了并不是一个人能移动", user.getName(), optFrom));
            }
            return;
        }

        if(isDoOpt(optFrom, DOOR1) || isDoOpt(optFrom, DOOR2)){
            resMsgs.add(user.getName() + "在门前看了看");
            return;
        }
        if(isDoOpt(optFrom, WINDOW1) || isDoOpt(optFrom, WINDOW1)){
            resMsgs.add(user.getName() + "在窗边看了看");
            return;
        }

        resMsgs.add(user.getName() + "正在进行神秘仪式");
    }


    public boolean isTable1InDoor1() {
        return table1InDoor1;
    }

    public boolean isTable2InDoor1() {
        return table2InDoor1;
    }

    public boolean isChair1InDoor1() {
        return chair1InDoor1;
    }

    public boolean isChair2InDoor1() {
        return chair2InDoor1;
    }

    public boolean isTable1InDoor2() {
        return table1InDoor2;
    }

    public boolean isTable2InDoor2() {
        return table2InDoor2;
    }

    public boolean isChair1InDoor2() {
        return chair1InDoor2;
    }

    public boolean isChair2InDoor2() {
        return chair2InDoor2;
    }

    public boolean isChair1In() {
        return chair1In;
    }

    public boolean isChair2In() {
        return chair2In;
    }

    public boolean isTable1In() {
        return table1In;
    }

    public boolean isTable2In() {
        return table2In;
    }
}
