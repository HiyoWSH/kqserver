package kq.server.gm.trpg;

public class Sence {
    private long time;
    private String msg;

    public Sence(long time, String msg) {
        this.time = time;
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String event(){
        return msg;
    }
}
