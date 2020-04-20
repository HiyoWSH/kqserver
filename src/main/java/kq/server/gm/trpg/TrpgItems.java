package kq.server.gm.trpg;

public class TrpgItems {
    public TrpgItems(String name) {
        this.name = name;
    }

    public TrpgItems(String name, String viewMse, String moveMsg) {
        this.name = name;
        this.viewMse = viewMse;
        this.moveMsg = moveMsg;
    }

    String name;
    String viewMse;
    String moveMsg;
    boolean isViewed = false;
    boolean isOpted = false;

    public String view(){
        return viewMse;
    }


    public String move() {
        return moveMsg;
    }

    public void opt() {
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }

    public boolean isOpted() {
        return isOpted;
    }

    public void setOpted(boolean opted) {
        isOpted = opted;
    }
}
