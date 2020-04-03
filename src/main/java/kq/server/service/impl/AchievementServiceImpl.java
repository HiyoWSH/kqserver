package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.AchievementService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static kq.server.util.MessageUtil.*;

@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    UserMapper userMapper;

    @Override
    public JSONObject doShowAchievement(Message message) {
        String command = message.getCommand();
        try{
            String name = command.replaceAll("查看成就", "").trim();
            if(StringUtils.isNotBlank(name)){
                for(Achievement achievement:Achievement.achievementList){
                    if(StringUtils.equals(name, achievement.getAchievement_name())){
                        JSONObject resjson = getResBase(message);
                        addJsonMessageWithEnter(resjson, "成就名称：" + achievement.getAchievement_name());
                        addJsonMessageWithEnter(resjson, "达成所需卡牌：" + achievement.getNeeded());
                        addJsonMessageWithEnter(resjson, "所需卡牌数量：" + (achievement.getNeed_count()>0?achievement.getNeed_count():achievement.getNeeded().split(",").length));
                        addJsonMessageWithEnter(resjson, "说明：" + achievement.getDescription());
                        return resjson;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        User user = userMapper.getUser(message.getUser_id());
        String[] achievements = user.getAchievementsArray();
        JSONObject resjson = getResBase(message);
        if (achievements.length == 0 || (achievements.length == 1 && StringUtils.isBlank(achievements[0]))){
            addJsonMessageWithEnter(resjson,"你还没有获得任何成就");
            return resjson;
        }
        addJsonMessageWithEnter(resjson,"你获得的成就如下");
        for(String a:achievements){
            if(StringUtils.isNotBlank(a)) {
                addJsonMessageWithEnter(resjson, "[" + a + "]");
            }
        }
        addJsonMessageWithEnter(resjson,"输入[查看成就 成就名]可以查看成就详情");
        return resjson;
    }
}
