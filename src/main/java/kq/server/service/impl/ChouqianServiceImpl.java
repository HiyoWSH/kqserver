package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.ChouqianService;
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

    @Override
    public JSONObject getChouqianRes(Message message) {
        JSONObject resjson = getResBase(message);
        int user_id = message.getUser_id();
        User user = userMapper.getUser(user_id);
        if(user == null){
            user = new User();
            user.setUser_id(user_id);
            userMapper.insertUser(user);
        }
        // 今日已抽过
        if(user.getLast_qian() != null && user.getLast_get_qian() != null
                && getTodayStartTime() < user.getLast_get_qian().getTime()){
            return getHadChouqianRes(resjson, user);
        }

        String qian = chouqian();
        resjson = getChouqianRes(resjson, qian);
        resjson = getCoinqianRes(resjson, qian, user);

        user.setLast_qian(qian);
        user.setLast_get_qian(new Date());
        userMapper.updateUser(user);
        return resjson;
    }

    private JSONObject getCoinqianRes(JSONObject resjson, String qian, User user) {
        int coin = 0;
        switch (qian) {
            case "大吉":
                addJsonMessageWithEnter(resjson, "今天运势不错哦(硬币+5)");
                coin = 5;break;
            case "小吉":
                addJsonMessageWithEnter(resjson, "今天似乎会有好运呢(硬币+3)");
                coin = 3;break;
            case "末吉":
                addJsonMessageWithEnter(resjson, "今天运势还行(硬币+1)");
                coin = 1;break;
            case "大凶":
                addJsonMessageWithEnter(resjson, "今天...老实的待在家里会比较好吧(硬币-1)");
                coin = -1;break;
            case " 凶 ":
            default:
                addJsonMessageWithEnter(resjson, "今天运势似乎不太好呢(硬币+0)");
                coin = 0;break;
        }
        userService.getCoins(user, coin);
        addJsonMessageWithEnter(resjson, "硬币数：" + user.getCoins());
        return resjson;
    }

    private JSONObject getHadChouqianRes(JSONObject resjson, User user){
        resjson = getChouqianRes(resjson, user.getLast_qian());
        resjson = addJsonMessage(resjson, "<既定的命运无法改变>");
        return resjson;
    }

    private JSONObject getChouqianRes(JSONObject resjson, String qian){
        String resmsg = chouqianMsg(qian);
        return addJsonMessage(resjson, resmsg);
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
            return "小吉";
        } else if(r >= 20){
            return "末吉";
        } else if(r >= 5){
            return " 凶 ";
        } else {
            return "大凶";
        }
    }

}
