package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.ChouqianService;
import kq.server.service.MiraiMessageSenderService;
import kq.server.service.UserService;
import kq.server.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import static kq.server.util.MessageUtil.*;
import static kq.server.util.TimeUtil.*;
import static kq.server.util.RandomUtil.*;

@Service
public class ChouqianServiceImpl implements ChouqianService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserService userService;

    @Autowired
    MiraiMessageSenderService miraiMessageSenderService;

    @Override
    public String getChouqianRes(User user) {
        StringBuilder stringBuilderRes = new StringBuilder();

        // 今日已抽过
        if(user.getLast_qian() != null && user.getLast_get_qian() != null
                && getTodayStartTime() < user.getLast_get_qian().getTime()){
            return getHadChouqianRes(user);
        }

        String qian = chouqian();
        stringBuilderRes.append(getChouqianRes(qian)).append("\n");
        stringBuilderRes.append(getCoinqianRes(qian, user));

        user.setLast_qian(qian);
        user.setLast_get_qian(new Date());
        userMapper.updateUser(user);
        return stringBuilderRes.toString();
    }

    private String getCoinqianRes(String qian, User user) {
        StringBuilder stringBuilderRes = new StringBuilder();
        int coin = 0;
        switch (qian) {
            case "大吉":
                stringBuilderRes.append("今天运势不错哦(硬币+5)").append("\n");
                coin = 5;break;
            case "中吉":
                stringBuilderRes.append("今天似乎会有好运呢(硬币+3)").append("\n");
                miraiMessageSenderService.sendImageWait("file:///F:/setudir/chouqian/%E4%B8%AD%E5%90%89.jpg");
                coin = 3;break;
            case "末吉":
                stringBuilderRes.append("今天运势还行(硬币+1)").append("\n");
                coin = 1;break;
            case "大凶":
                stringBuilderRes.append("今天...老实的待在家里会比较好吧(硬币-1)").append("\n");
                coin = -1;
                miraiMessageSenderService.sendImageWait("file:///F:/setudir/chouqian/%E5%A4%A7%E5%87%B6.jpg");
                break;
            case " 凶 ":
            default:
                stringBuilderRes.append("今天运势似乎不太好呢(硬币+0)").append("\n");
                coin = 0;
                break;
        }
        userService.getCoins(user, coin);
        stringBuilderRes.append("硬币数：" + user.getCoins());
        return stringBuilderRes.toString();
    }

    private String getHadChouqianRes(User user){
        return getChouqianRes(user.getLast_qian()) + "\n<既定的命运无法改变>";
    }

    private String getChouqianRes(String qian){
        return chouqianMsg(qian);
    }

    private String chouqianMsg(String qian){
        String resmsg = "\n让栗小栗来帮你抽一签吧\n" +
                "=====\n" +
                "=" + qian + "=\n" +
                "=====\n";
        return resmsg;
    }

    private String chouqian(){
        int r = getRandom().nextInt(100);
        logger.info("[Chouqian random] = " + r);
        if(r >= 90){
            return "大吉";
        } else if(r >= 50){
            return "中吉";
        } else if(r >= 20){
            return "末吉";
        } else if(r >= 5){
            return " 凶 ";
        } else {
            return "大凶";
        }
    }

    private String enterBuilder(String... strs){
        StringBuilder stringBuilderRes = new StringBuilder();
        for(String str:strs){
            stringBuilderRes.append(str).append("\n");
        }
        return stringBuilderRes.toString();
    }

}
