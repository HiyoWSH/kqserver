package kq.server.gm.trpgmode;

public class Sence {

    /**
     *  触发时间
     */
    protected long timeScope;
    /**
     *  触发目标模组
     */
    protected TrpgMode targetMode;

    protected String senceMessage;

    public Sence() {
    }

    public Sence(long timeScope, String senceMessage, TrpgMode targetMode) {
        this.timeScope = timeScope;
        this.targetMode = targetMode;
        this.senceMessage = senceMessage;
    }

    /**
     * 事件
     */
    public String event(){
        return senceMessage;
    }

    /**
     * 事件
     * @param from 事件来源
     * @param to   事件目标
     */
    public String event(Object from, Object to){
        return "";
    }

    public long getTimeScope() {
        return timeScope;
    }

    public void setTimeScope(long timeScope) {
        this.timeScope = timeScope;
    }

    public TrpgMode getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(TrpgMode targetMode) {
        this.targetMode = targetMode;
    }

    public String getSenceMessage() {
        return senceMessage;
    }

    public void setSenceMessage(String senceMessage) {
        this.senceMessage = senceMessage;
    }
}
