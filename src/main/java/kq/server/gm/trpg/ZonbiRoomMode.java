package kq.server.gm.trpg;

import kq.server.bean.User;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ZonbiRoomMode extends TrpgBaseMode {

    private static final Logger logger = Logger.getLogger(ZonbiRoomMode.class);
    private TrpgItems[][] map = new TrpgItems[3][4];
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


    @Override
    public void modeLoaded() {
        this.description = "模组MODE2\n" +
                "目标：？\n" +
                "失败条件：？\n" +
                "指令列表：\n" +
                "观察 [对象1]\n" +
                "操作 [对象1]\n" +
                "操作 [对象1] [对象2]  （使用对象1操作对象2）\n" +
                "移动 [坐标]           （移动 1,1）\n" +
                "背包                  （查看拥有的道具）\n" +
                "\n" +
                "当你意识到的时候已经在这样一间房间之中，直觉告诉你这里并不安全。\n" +
                "地图：\n" +
                "门1一一一一一一门2\n" +
                "一一一一一一一一一\n" +
                "一一桌1一一桌2一一\n" +
                "一一椅1一一椅2一一\n" +
                "一一一一一一一一一\n" +
                "一一窗1一一窗2一一";
        sence = new ArrayList();

        TrpgItems t1 = new TrpgItems(TABLE1);
        TrpgItems t2 = new TrpgItems(TABLE2);
        TrpgItems c1 = new TrpgItems(CHAIR1);
        TrpgItems c2 = new TrpgItems(CHAIR2);
        TrpgItems d1 = new TrpgItems(DOOR1);
        TrpgItems d2 = new TrpgItems(DOOR2);
        TrpgItems w1 = new TrpgItems(WINDOW1);
        TrpgItems w2 = new TrpgItems(WINDOW2);
        TrpgItems jd = new TrpgItems(剪刀);
        TrpgItems kaki = new TrpgItems(钥匙);

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
    }

    @Override
    void doOpt(User user, String optFrom, String optTo) {

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
}
