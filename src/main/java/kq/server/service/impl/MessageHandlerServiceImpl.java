package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.*;
import kq.server.threads.MessageSender;
import kq.server.threads.TrpgRunner;
import kq.server.util.DealRegexLib;
import kq.server.util.MessageUtil;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MessageHandlerServiceImpl implements MessageHandlerService {

    private static Logger logger = Logger.getLogger(MessageHandlerServiceImpl.class);

    @Autowired
    CardService cardService;
    @Autowired
    ChouqianService chouqianService;
    @Autowired
    AchievementService achievementService;
    @Autowired
    StoryService storyService;
    @Autowired
    UserService userService;
    @Autowired
    UserMapper userMapper;

    TrpgRunner trpgRunner;

    @Override
    public void doDeal(Message message) {
        String command = message.getCommand();
        try {
            if(trpgRunner != null && trpgRunner.isAlive()){
                for(User user:trpgRunner.getUserlist()){
                    if(user.getUser_id() == message.getUser_id()){
                        trpgRunner.getUserOption().get(message.getUser_id()).add(message.getCommand());
                        return;
                    }
                }
            }

            JSONObject resjson = null;
            if(command.startsWith("功能") || command.startsWith("菜单") || command.startsWith("help") || command.startsWith("帮助")){
                resjson = doDealMenu(message);
            } else if(command.contains("抽签")){
                resjson = chouqianService.getChouqianRes(message);
            } else if(command.contains("抽卡")){
                resjson = cardService.doDealChouka(message);
            } else if(command.contains("查看卡牌") || command.contains("查看卡片")){
                resjson = cardService.doShowCard(message);
            } else if(command.contains("卡牌商店")){
                resjson = cardService.doShowCardShop(message);
            } else if(command.contains("交换卡牌")){
                resjson = cardService.doCardExchange(message);
            } else if(command.contains("我的资料")){
                resjson = showUserInfo(message);
            } else if(command.contains("查看成就")){
                resjson = achievementService.doShowAchievement(message);
            } else if(command.contains("听故事") || command.contains("讲故事")) {
                resjson = storyService.doDealStory(message);
            } else if(command.contains("TRPG MODE1")){
                User user = userMapper.getUser(message.getUser_id());
                user.setName(message.getBody().getJSONObject("sender").getString("nickname"));
                if(trpgRunner != null && trpgRunner.isAlive()) {
                    trpgRunner.addUser(user);
                    resjson = MessageUtil.getNormalRes(message, String.format("%s 加入了模组，当前模组人数 %d人", user.getName(), trpgRunner.getUserlist().size()));
                } else {
                    trpgRunner = new TrpgRunner(message, user);
                    trpgRunner.start();
                }
            } else if(command.contains("色图")){
                // TUDO pro
                resjson = doDealNormal(message, command);
            } else {
                resjson = doDealNormal(message, command);
            }
            message.setResbody(resjson);
            //发送
            MessageSender.sendMessage(message);
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e);
        }
    }

    private JSONObject showUserInfo(Message message) {
        JSONObject resjson = MessageUtil.getResBase(message);
        // TODO
        return resjson;
    }

    /**
     * 菜单
     * @param message
     * @return
     */
    private JSONObject doDealMenu(Message message) {
        JSONObject resjson = MessageUtil.getResBase(message);
        resjson = MessageUtil.addJsonMessageWithEnter(resjson,
                "栗小栗的指令帮助喵~\n" +
                        "指令基本格式：[指令] [参数1] [参数2] ....\n" +
                        "抽签指令：\n" +
                        "  1.抽签\n" +
                        "卡牌指令：\n" +
                        "  1.抽卡[抽卡数]\n" +
                        "  2.查看卡牌\n" +
                        "  3.查看卡牌 [卡牌名称]\n" +
                        "  4.卡牌商店\n" +
                        "  5.交换卡牌 [卡牌名称]\n" +
                        "用户指令：\n" +
                        "  1.我的资料\n" +
                        "成就指令：\n" +
                        "  1.查看成就\n" +
                        "  2.查看成就 [成就名称]\n" +
                        "点歌指令：\n" +
                        "  1.网易 [歌曲名]  (注：推荐使用)\n" +
                        "  2.点歌 [歌曲名]  (注：QQ音乐)\n" +
                        "听故事指令：\n" +
                        "  1.听故事\n" +
                        "  2.听故事 [关键字]\n" +
                        "模组指令(测试中)：\n" +
                        "  TRPG MODE1\n" +
                        "吐槽：\n" +
                        "  小心栗小栗吐槽你哦\n" +
                        "爱你❤：\n" +
                        "  栗小栗❤你哦\n" +
                        "其它指令：\n" +
                        "  你猜喵");
        return resjson;
    }

    /**
     * 未定义命令
     * @param message
     * @param command
     * @return
     */
    private JSONObject doDealNormal(Message message, String command) {
        JSONObject resjson = MessageUtil.getResBase(message);
        if(doDealRegex(command, resjson)){
            return resjson;
        }
        if(RandomUtil.getNextInt(100) >= 95){
            resjson = MessageUtil.getNormalRes(message, "@栗小栗 并回复\"菜单\"可以查询栗小栗的使用方式哦");
            return resjson;
        }
        String chp = getChp();
        if(chp != null){
            resjson = MessageUtil.getNormalRes(message, chp);
        } else {
            resjson = MessageUtil.getNormalRes(message);
        }
        return resjson;
    }

    /**
     * 正则匹配
     * @param command
     * @param resjson
     * @return
     */
    private boolean doDealRegex(String command, JSONObject resjson) {
        String regexRes = DealRegexLib.getRes(command);
        if(StringUtils.isNotBlank(regexRes)){
            resjson = MessageUtil.addJsonMessageWithEnter(resjson, regexRes);
            return true;
        }
        return false;
    }

    /**
     * 从外部网站获得chp回复
     * @return
     */
    private String getChp(){
        try {
            String url = "https://chp.shadiao.app/api.php";
            RestTemplate restTemplate = new RestTemplate();
            String res = restTemplate.getForObject(url, String.class);
            logger.info("GETCHP" + res);
            return res;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
