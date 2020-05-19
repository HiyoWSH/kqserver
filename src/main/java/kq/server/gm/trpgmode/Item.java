package kq.server.gm.trpgmode;

public class Item {
    /**
     * 物品名
     */
    String name;
    /**
     * 数量
     */
    int count;
    /**
     * 被操作次数
     */
    int optTimes;
    /**
     * 被观察次数
     */
    int viewTimes;
    /**
     * 被移动次数
     */
    int moveTimes;
    String viewMse;
    String moveMsg;

    public Item(){}
    public Item(String name){
        this.name = name;
    }
    public Item(String name, String viewMse, String moveMsg) {
        this.name = name;
        this.viewMse = viewMse;
        this.moveMsg = moveMsg;
    }

    public String view(){
        return viewMse;
    }


    public String move() {
        return moveMsg;
    }

    public void opt() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getOptTimes() {
        return optTimes;
    }

    public void setOptTimes(int optTimes) {
        this.optTimes = optTimes;
    }

    public int getViewTimes() {
        return viewTimes;
    }

    public void setViewTimes(int viewTimes) {
        this.viewTimes = viewTimes;
    }

    public int getMoveTimes() {
        return moveTimes;
    }

    public void setMoveTimes(int moveTimes) {
        this.moveTimes = moveTimes;
    }

    public String getViewMse() {
        return viewMse;
    }

    public void setViewMse(String viewMse) {
        this.viewMse = viewMse;
    }

    public String getMoveMsg() {
        return moveMsg;
    }

    public void setMoveMsg(String moveMsg) {
        this.moveMsg = moveMsg;
    }
}
