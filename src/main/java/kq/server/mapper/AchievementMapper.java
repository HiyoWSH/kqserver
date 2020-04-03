package kq.server.mapper;

import kq.server.bean.Achievement;

import java.util.List;

public interface AchievementMapper {
    List<Achievement> getAchievements();
    Achievement getAchievementById(int id);
    void insertAchievement(Achievement achievement);
}
