package kq.server.gm.trpgmode.mode2.sence;

import kq.server.gm.trpgmode.Sence;
import kq.server.gm.trpgmode.TrpgMode;
import kq.server.gm.trpgmode.TrpgUtil;
import kq.server.gm.trpgmode.mode2.ZonbiRoomMode;

import java.util.List;
import java.util.Map;

public class KinokSence extends Sence {

    int dleft = 2;
    int kinokJpoint = 31;
    ZonbiRoomMode targetMode;

    public KinokSence(long l, TrpgMode targetMode) {
        this.setTimeScope(l);
        this.targetMode = (ZonbiRoomMode) targetMode;
    }

    @Override
    public String event(){


        List resMsgs = targetMode.getResMsgs();

        if(this.getTimeScope() > 8*60*1000){
            resMsgs.add("不知过了多少时间，撞门的声音渐渐平息，门外的声音渐渐远去~~~");
            if(targetMode.isTable2InDoor1()) {
                resMsgs.add("HAPPY END - 001 成功存活");
            } else {
                resMsgs.add("HAPPY END - 002 幸运的生还者");
            }
            return "";
        }

        resMsgs.add("“咚~”门1发出了被撞击的巨大响声");

        if(targetMode.isChair1InDoor1()){
            kinokJpoint -= 5;
            resMsgs.add("椅1在门1口，稍微抵挡了一些冲击");
        }
        if(targetMode.isChair2InDoor1()){
            kinokJpoint -= 5;
            resMsgs.add("椅2在门1口，稍微抵挡了一些冲击");
        }
        if(targetMode.isTable1InDoor1()){
            kinokJpoint -= 5;
            resMsgs.add("桌1在门1口，稍微抵挡了一些冲击");
        }
        if(targetMode.isTable2InDoor1()){
            kinokJpoint -= 30;
            resMsgs.add("椅2在门1口，似乎能抗得住");
        }
        int judge = TrpgUtil.getTrpgJudge();
        String judgeMsg = TrpgUtil.judgeMsg(judge, kinokJpoint);
        resMsgs.add("判定：" + judge + " (31-?)) ... " + judgeMsg);
        if(judgeMsg.contains("失败")){
            int n = 1;
            if(judgeMsg.contains("大失败")){
                n = 3;
            }
            KinokSence nkc = new KinokSence(this.getTimeScope() + n * 20 * 1000, this.targetMode);
            nkc.setDleft(getDleft());
            this.targetMode.getSences().put("kinok2", nkc);
        } else {
            int n = 1;
            if(judgeMsg.contains("大成功")){
                n = 2;
            }
            setDleft(getDleft() - n);
            if(dleft > 0){
                KinokSence nkc = new KinokSence(this.getTimeScope() + 20 * 1000, this.targetMode);
                nkc.setDleft(getDleft());
                this.targetMode.getSences().put("kinok2", nkc);
                this.targetMode.getItems().get("门1").setViewMse("木质拉门，门上出现了一条长长的裂痕，摇摇欲坠");
                resMsgs.add("“咚~”门1上出现了一条长长的裂痕，摇摇欲坠");
            } else {
                this.targetMode.getItems().get("门1").setViewMse("木质拉门，已损坏");
                resMsgs.add("“咚~”门1被撞开，在你面前出现的是... to be continue ... ");
                resMsgs.add("BAD END - 002 闯入的xx");
            }
        }

        return "";
    }

    public int getDleft() {
        return dleft;
    }

    public void setDleft(int dleft) {
        this.dleft = dleft;
    }
}
