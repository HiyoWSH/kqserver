package kq.server.gm.trpgmode.mode1;

import kq.server.gm.trpgmode.Sence;

public class Mode1Sence extends Sence {
    private String msg;

    public Mode1Sence(long time, String msg) {
        this.timeScope = time;
        this.msg = msg;
    }

    public long getTime() {
        return timeScope;
    }

    public void setTime(long time) {
        this.timeScope = time;
    }

    @Override
    public String event(){
        return msg;
    }
}
