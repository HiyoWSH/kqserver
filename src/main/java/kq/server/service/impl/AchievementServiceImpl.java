package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Achievement;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.AchievementService;
import kq.server.service.ImageService;
import kq.server.service.MiraiMessageSenderService;
import kq.server.threads.MiraiSender;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static kq.server.util.MessageUtil.*;

@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    MiraiMessageSenderService miraiMessageSenderService;

    @Override
    public String doShowAchievement(User user, String command) {
        StringBuilder builder = new StringBuilder();
        try{
            String name = command.replaceAll("查看成就", "").trim();
            if(StringUtils.isNotBlank(name)){
                for(Achievement achievement:Achievement.achievementList){
                    if(StringUtils.equals(name, achievement.getAchievement_name())){
                        builder.append("\n").append("成就名称：" + achievement.getAchievement_name());
                        builder.append("\n").append("达成所需卡牌：" + achievement.getNeeded());
                        builder.append("\n").append("所需卡牌数量：" + (achievement.getNeed_count()>0?achievement.getNeed_count():achievement.getNeeded().split(",").length));
                        builder.append("\n").append("说明：" + achievement.getDescription());
                        if(achievement.getImagepath() != null) {
                            miraiMessageSenderService.sendImageWait(achievement.getImagepath());
                        }
                        return builder.toString();
                    }
                }
            }
        } catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }

        String[] achievements = user.getAchievementsArray();
        if (achievements.length == 0 || (achievements.length == 1 && StringUtils.isBlank(achievements[0]))){
            builder.append("你还没有获得任何成就");
            return builder.toString();
        }
        builder.append("\n").append("你获得的成就如下");
        for(String a:achievements){
            if(StringUtils.isNotBlank(a)) {
                builder.append("\n").append("[" + a + "]");
            }
        }
        builder.append("\n").append("输入查看成就 [成就名]可以查看成就详情");
        return builder.toString();
    }
}
