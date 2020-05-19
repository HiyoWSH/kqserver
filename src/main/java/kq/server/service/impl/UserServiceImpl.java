package kq.server.service.impl;

import kq.server.bean.User;
import kq.server.mapper.AchievementMapper;
import kq.server.mapper.CardMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static kq.server.util.TimeUtil.getTodayStartTime;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    CardMapper cardMapper;
    @Autowired
    AchievementMapper achievementMapper;

    @Override
    public boolean costCoins(User user, int count) {
        if(user == null){
            return false;
        }
        if(user.getCoins() >= count){
            user.setCoins(user.getCoins() - count);
            userMapper.updateUser(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean costCoins(int user_id, int count) {
        return costCoins(userMapper.getUser(user_id), count);
    }

    @Override
    public boolean getCoins(User user, int count) {
        if(user == null){
            return false;
        }
        user.setCoins(user.getCoins() + count);
        userMapper.updateUser(user);
        return true;
    }

    @Override
    public boolean getCoins(int user_id, int count) {
        return getCoins(userMapper.getUser(user_id), count);
    }


    @Override
    public String showUserInfo(User user) {
        String infoModel = "用户id：%d\n" +
                "今日抽签：%s\n" +
                "硬币数：%d\n" +
                "卡牌收集数：%d/%d\n" +
                "成就达成数：%d/%d";
        String qian;
        if(user.getLast_qian() != null && user.getLast_get_qian() != null
                && getTodayStartTime() < user.getLast_get_qian().getTime()){
            qian = user.getLast_qian();
        } else {
            qian = "今日未抽签";
        }
        int userCardCount = cardMapper.getUserCards(user.getUser_id()).size();
        int cardCount = cardMapper.getCards().size();
        int userAchieveCount = user.getAchievementsArray().length>0?user.getAchievementsArray().length-1:user.getAchievementsArray().length;
        int achieveCount = achievementMapper.getAchievements().size();
        return String.format(infoModel, user.getUser_id(),qian,user.getCoins(),userCardCount,cardCount,userAchieveCount,achieveCount);
    }

}
