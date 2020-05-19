package kq.server.bean;

import java.util.List;

public class Achievement {

    public static List<Achievement> achievementList;

    int id;
    private String achievement_name;
    private String needed;
    private String description;
    int need_count;
    private String imagepath;

    public Achievement() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAchievement_name() {
        return achievement_name;
    }

    public void setAchievement_name(String achievement_name) {
        this.achievement_name = achievement_name;
    }

    public String getNeeded() {
        return needed;
    }

    public void setNeeded(String needed) {
        this.needed = needed;
    }

    public String[] getNeededArray(){
        return needed.split(",");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNeed_count() {
        return need_count;
    }

    public void setNeed_count(int need_count) {
        this.need_count = need_count;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }
}
