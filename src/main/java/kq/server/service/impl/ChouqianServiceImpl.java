package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.ChouqianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import static kq.server.util.MessageUtil.*;
import static kq.server.util.TimeUtil.*;

@Service
public class ChouqianServiceImpl implements ChouqianService {

    @Autowired
    UserMapper userMapper;

    @Override
    public JSONObject getChouqianRes(Message message) {
        JSONObject resjson = getResBase(message);
        int user_id = message.getUser_id();
        User user = userMapper.getUser(user_id);
        if(user == null){
            user = new User();
            user.setUser_id(user_id);
        }
        // 今日已抽过
        if(user.getLast_qian() != null && user.getLast_get_qian() != null
                && getTodayStartTime() < user.getLast_get_qian().getTime()){
            String resmsg = "\n让栗小栗来帮你抽一签吧\n" +
                    "=====\n" +
                    "=" + user.getLast_qian() + "=\n" +
                    "=====\n";
            resjson = addJsonMessage(resjson, resmsg);
            resjson = addJsonMessage(resjson, "<既定的命运无法改变>");
            return resjson;
        }

        String qian = "";
        int r = new Random(System.currentTimeMillis()).nextInt(100);
        logger.info("[Chouqian random] = " + r);
        if(r >= 90){
            qian = "大吉";
        } else if(r >= 50){
            qian = "小吉";
        } else if(r >= 20){
            qian = "末吉";
        } else if(r >= 5){
            qian = " 凶 ";
        } else {
            qian = "大凶";
        }
        String resmsg = "\n让栗小栗来帮你抽一签吧\n" +
                "=====\n" +
                "=" + qian + "=\n" +
                "=====\n";
        resjson = addJsonMessage(resjson, resmsg);

        user.setLast_qian(qian);
        user.setLast_get_qian(new Date());
        userMapper.updateUser(user);
        return resjson;
    }

}
