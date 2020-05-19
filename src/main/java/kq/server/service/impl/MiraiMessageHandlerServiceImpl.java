package kq.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.ImageId;
import kq.server.bean.MiraiMessage;
import kq.server.bean.User;
import kq.server.gm.trpgmode.mode1.HatuneRoomMode;
import kq.server.gm.trpgmode.mode2.ZonbiRoomMode;
import kq.server.mapper.ImageIdMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.*;
import kq.server.threads.MiraiSender;
import kq.server.threads.TrpgRunner;
import kq.server.threads.TrpgRunnerForMirai;
import kq.server.util.DownloadURLFile;
import kq.server.util.MessageUtil;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import static kq.server.util.MiraiMessageUtil.*;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MiraiMessageHandlerServiceImpl implements MiraiMessageHandlerService {

    private static Logger logger = Logger.getLogger(MiraiMessageHandlerServiceImpl.class);

    @Autowired MiraiMessageSenderService miraiMessageSenderService;

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    UserMapper userMapper;
    @Autowired
    ImageIdMapper imageIdMapper;
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
    ImageService imageService;
    @Autowired
    NormalService normalService;

    TrpgRunnerForMirai trpgRunner;

    @Override
    public String doDeal(JSONObject body) {

        try {
            String command = getText(body);
            if(StringUtils.isBlank(command)){
                return "";
            }
            User user = getUser(body);
            JSONObject resjson = new JSONObject();
            resjson.put("target", getTarget(body));
            String commandRes = "";
            long targetAtId = 0;
            String mes = "@栗小栗";

            if (isAtMe(body, 627985299, mes) && StringUtils.isNotBlank(command)) {
                command = command.replaceAll(mes,"").trim();
                if(trpgRunner != null && trpgRunner.isAlive()){
                    for(User trpgUser:trpgRunner.getUserlist()){
                        if(trpgUser.getUser_id() == user.getUser_id()){
                            trpgRunner.getUserOption().get(user.getUser_id()).add(command);
                            return "";
                        }
                    }
                }

                if(command.contains("TRPG MODE")){
                    commandRes = getTrpgModeRes(body, user, command);
                } else {
                    commandRes = getCommandRes(user, command);
                }
                targetAtId = user.getUser_id();
            } else {
                if(command.contains("色图") || command.contains("涩图") || command.contains("setu") || command.contains("瑟图") ){
                    sendSeTu(body);
                    return "";
                }
                if (RandomUtil.getNextInt(100) > 96) {
                    try {
                        commandRes = normalService.getTuLingRes(command);
                    } catch (Exception e){
                        logger.warn(e);
                    }
                }
            }

            if(StringUtils.isNotBlank(commandRes)) {
                resjson.put("messageChain", createMessageTextChain(commandRes));
                miraiMessageSenderService.sendMessage(getType(body), resjson, targetAtId);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String getTrpgModeRes(JSONObject body, User user, String command) {
        MiraiMessage message = new MiraiMessage(getType(body), body.getJSONObject("sender"), body.getJSONArray("messageChain"), body);
        if(command.contains("TRPG MODE1")){
            user.setName(String.valueOf(user.getUser_id()));
            if(trpgRunner != null && trpgRunner.isAlive()) {
                trpgRunner.addUser(user);
                return String.format("%s 加入了模组，当前模组人数 %d人", user.getName(), trpgRunner.getUserlist().size());
            } else {
                trpgRunner = new TrpgRunnerForMirai(message, new HatuneRoomMode(), user);
                trpgRunner.setMiraiMessageSenderService(miraiMessageSenderService);
                trpgRunner.start();
            }
            return "模组维护中";
        } else if(command.contains("TRPG MODE2")){
            user.setName(String.valueOf(user.getUser_id()));
            if(trpgRunner != null && trpgRunner.isAlive()) {
                trpgRunner.addUser(user);
                return String.format("%s 加入了模组，当前模组人数 %d人", user.getName(), trpgRunner.getUserlist().size());
            } else {
                trpgRunner = new TrpgRunnerForMirai(message, new ZonbiRoomMode(), user);
                trpgRunner.setMiraiMessageSenderService(miraiMessageSenderService);
                trpgRunner.start();
            }
            return "模组维护中";
        } else {
            return "模组不存在";
        }
    }

    private void sendSeTu(JSONObject body){
        // 通过上传 无法上传 未使用
//                String path = "C:\\Users\\wangsh\\Pictures\\QQ图片20200429090556.png";
//                ImageId imageId = imageIdMapper.getImageIdByPass(path);
//                if(imageId == null){
//                    imageId = new ImageId();
//                    String res = uploadImage(path);
//                    logger.info(res);
//                    imageId.setImageid(res);
//                    imageId.setPath(path);
//                    imageIdMapper.insertImageId(imageId);
//                }
//                JSONObject json = new JSONObject();
//                json.put("type","Image");
//                json.put("text",JSONObject.parseObject(imageId.getImageid()).get("imageId"));
//
//                JSONArray array = createMessageChain("");
//                array.add(json);
//                JSONObject resjson = new JSONObject();
//                resjson.put("target", getTarget(body));
//                resjson.put("messageChain", array);
//                String type = getType(body);
//                switch (type) {
//                    case "GroupMessage":
//                        sendGroupMessage(resjson);
//                        break;
//                    case "FriendMessage":
//                        sendFriendMessage(resjson);
//                        break;
//                }
//                return "";
        if(RandomUtil.getNextInt(100) > 60) {
            logger.info("image source 1");
            String path = "file:///" + imageService.getImage();
            JSONObject resjson = new JSONObject();
            resjson.put("group", getTarget(body));

            sendImage(resjson, path);
        } else if(RandomUtil.getNextInt(100) > 60) {
            logger.info("image source 2");
            String randomImgRes = getRandomImageFromRemote1();
            try{
                DownloadURLFile.downloadFromUrl(randomImgRes, "F:\\setudir\\fromNet");
            } catch (Exception e){
                e.printStackTrace();
            }
            JSONObject resjson = new JSONObject();
            resjson.put("group", getTarget(body));

            sendImage(resjson, randomImgRes);
        } else {
            logger.info("image source 3");
            String randomImgRes = getRandomImageFromRemote2();
            try{
                DownloadURLFile.downloadImgByNet(randomImgRes, "F:\\setudir\\fromNet", String.valueOf(System.currentTimeMillis()) + ".jpg");
            } catch (Exception e){
                e.printStackTrace();
            }
            JSONObject resjson = new JSONObject();
            resjson.put("group", getTarget(body));

            sendImage(resjson, randomImgRes);
        }
    }

    private String getRandomImageFromRemote1(){
        String url = "http://img.xjh.me/random_img.php";
        String randomImgRes = restTemplate.getForObject(url, String.class);
        Pattern pattern = Pattern.compile(" src=\"//.*?\"");
        Matcher matcher = pattern.matcher(randomImgRes);
        if (matcher.find()) {
            randomImgRes = matcher.group();
        }
        randomImgRes = randomImgRes.replaceAll(" src=\"", "");
        randomImgRes = randomImgRes.replaceAll("\"", "");
        randomImgRes = "http:" + randomImgRes;
        return randomImgRes;
    }

    private String getRandomImageFromRemote2(){
        String url = "https://picloli.com/?random";
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent","Chrome/69.0.3497.81 Safari/537.36");
        HttpEntity requestEntity=new HttpEntity("", headers);
        ResponseEntity<String> rss = restTemplate.exchange(url, HttpMethod.GET, requestEntity, 	String.class, new HashMap<>());
        String randomImgRes = rss.getBody();
        Pattern pattern = Pattern.compile("url: \"https://img1\\.picloli-1\\.xyz.*?\\.jpg");
        Matcher matcher = pattern.matcher(randomImgRes);
        if (matcher.find()) {
            randomImgRes = matcher.group();
        }
        randomImgRes = randomImgRes.replaceAll("url: \"","");
        return randomImgRes;
    }

    @Override
    public synchronized void sendImage(JSONObject targetJson, String imageUrl){
        ImageId imageId = imageIdMapper.getImageIdByPass(imageUrl);
        JSONArray array = new JSONArray();
        array.add(imageUrl);
        targetJson.put("urls", array);
        if(imageId != null){
            targetJson.put("imageId", imageId.getImageid());
        }
        String res = miraiMessageSenderService.sendImageMessage(targetJson);
        if (imageId == null) {
            imageId = new ImageId();
            imageId.setPath(imageUrl);
            imageId.setImageid(JSONArray.parseArray(res).getString(0));
            imageIdMapper.insertImageId(imageId);
        }
    }

    private String getCommandRes(User user, String command) {
        if(command.startsWith("功能") || command.startsWith("菜单") || command.startsWith("help") || command.startsWith("帮助")){
            return normalService.doDealMenu();
        } else if(command.contains("抽签")){
            return chouqianService.getChouqianRes(user);
        } else if(command.contains("抽卡")){
            return cardService.doDealChouka(user,command);
        } else if(command.contains("查看卡牌") || command.contains("查看卡片")){
            return cardService.doShowCard(user, command);
        } else if(command.contains("卡牌商店")){
            return cardService.doShowCardShop(user);
        } else if(command.contains("交换卡牌")){
            return cardService.doCardExchange(user, command);
        } else if(command.contains("查看成就")){
            return achievementService.doShowAchievement(user, command);
        } else if(command.contains("听故事") || command.contains("讲故事")) {
            return storyService.doDealStory(command);
        } else if(command.contains("资料")){
            return userService.showUserInfo(user);
        } else {
            return normalService.getNormalResStr(user,command);
        }
    }

    private User getUser(JSONObject body) {
        User user = userMapper.getUser(body.getJSONObject("sender").getInteger("id"));
        if(user == null){
            user = new User();
            user.setUser_id(body.getJSONObject("sender").getInteger("id"));
            userMapper.insertUser(user);
        }
        return user;
    }
}
