package kq.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.ImageId;
import kq.server.bean.MiraiMessage;
import kq.server.bean.User;
import kq.server.config.Configuation;
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

    String localFromNet = "F:\\setudir\\localsetu\\fromNet";
    String setuFormNetR18 = "F:\\setudir\\fromNetR18";
//    String setuFormNet = "F:\\setudir\\fromNet";
//    String localFromNet = "/yori/kqserver/setu/local";
//    String setuFormNetR18 = "/yori/kqserver/setu/r18";
//    String setuFormNet = "/yori/kqserver/setu/local";
    boolean setuMode = true;
    long[] setuTarget = new long[]{};
    boolean randomReplyMode = true;
    long[] randomReplyTarget = new long[]{};
    String[] setuImgids = new String[]{"{762E153B-0264-AF37-002E-D0A1AD0CD004}.mirai", "{C4434875-EDB7-15F5-9EED-1CAB6428641B}.mirai"
            , "{06A609AD-21AB-EF20-6812-55D331058228}.mirai", "{27186B5D-63CE-9DA0-EB57-C16BE14FFA14}.mirai"
            , "{F61593B5-5B98-1798-3F47-2A91D32ED2FC}.mirai", "{CB356B43-7D29-B68A-562C-682F3837C4E7}.mirai"};
    String[] bugouseImgids = new String[]{"{C2680B48-A10B-1A35-62F4-635E5E0C7965}.mirai"};

    long[] dealSetuGroup = {287961148L, 329630396L, 532559793L};
    long[] dealRdResGroup = {532559793L};
    long[] dealMenuGroup = {287961148L, 329630396L, 532559793L};

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

            String commandRes = "";
            String setuRes = "";
            String menuRes = "";
            if(inArray(getTarget(body), dealSetuGroup)){
                setuRes = doSetuDeal(body);
            }

            if(inArray(getTarget(body), dealMenuGroup)){
                menuRes = doMenuDeal(body);
            }

            if(StringUtils.isNotBlank(setuRes)){
                commandRes = setuRes;
            } else if (StringUtils.isNotBlank(menuRes)) {
                commandRes = menuRes;
            }

            if(StringUtils.isNotBlank(commandRes)) {

                User user = getUser(body);
                JSONObject resjson = new JSONObject();
                resjson.put("target", getTarget(body));
                long targetAtId = 0;
                String mes = Configuation.getMes();
                long me = Configuation.getMe();
                if (isAtMe(body, me, mes)) {
                    targetAtId = user.getUser_id();
                }

                resjson.put("messageChain", createMessageTextChain(commandRes));
                miraiMessageSenderService.sendMessage(getType(body), resjson, targetAtId);
            }
        } catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }
        return "";
    }

    private String doMenuDeal(JSONObject body) {
        String command = getText(body);
        User user = getUser(body);
        JSONObject resjson = new JSONObject();
        resjson.put("target", getTarget(body));
        String commandRes = "";
        String mes = Configuation.getMes();
        long me = Configuation.getMe();

        if (isAtMe(body, me, mes) && StringUtils.isNotBlank(command)) {
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
            // 检测成就
            cardService.checkAchievement(user, body);
        } else {
            if(randomReplyMode && inArray(getTarget(body), dealRdResGroup)) {
                if (RandomUtil.getNextInt(100) > 96) {
                    try {
                        commandRes = normalService.getTuLingRes(command);
                    } catch (Exception e) {
                        logger.warn(e);
                    }
                }
            }
        }
        return commandRes;
    }

    private String doSetuDeal(JSONObject body) {
        if(setuMode) {
            String command = getText(body);
            String imgId = getImage(body);
            if (inArray(imgId, setuImgids)) {
                sendSeTu(body);
            } else if (inArray(imgId, bugouseImgids)) {
                sendSeTuH(body);
            } else if (StringUtils.isNotBlank(command)) {
                try {
                    if (command.contains("色图") || command.contains("涩图") || command.contains("setu") || command.contains("瑟图")) {
                        sendSeTu(body);
                    }
                    if (command.contains("不够色") || command.contains("不够涩") || command.contains("不够瑟")) {
                        sendSeTuH(body);
                        return "";
                    }
                } catch (Exception e) {
                    logger.error(e);
                    String commandRes = "涩图，涩图它没有了";
                    return commandRes;
                }
            }
        }
        return "";
    }

    private boolean inArray(String imgId, String[] strs) {
        for (String s:strs){
            if(StringUtils.equalsIgnoreCase(imgId, s)){
                return true;
            }
        }
        return false;
    }
    private boolean inArray(long id, long[] ids) {
        for (long i:ids){
            if(i == id){
                return true;
            }
        }
        return false;
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
        if(RandomUtil.getNextInt(100) > 20) {
            logger.info("image source 1");
            String path = imageService.getImage();
//            String path = "file:///" + imageService.getImage();
//            String path = "file:///yori/localsetu" + imageService.getImage().substring(20).replaceAll("\\\\", "/");//linux用
            JSONObject resjson = new JSONObject();
            resjson.put("group", getTarget(body));

            sendImage(resjson, path);
        } else
        if(RandomUtil.getNextInt(100) > 50) {
            logger.info("image source 2");
            String randomImgRes = getRandomImageFromRemote1();
            try{
                randomImgRes = DownloadURLFile.downloadFromUrl(randomImgRes, localFromNet);
            } catch (Exception e){
                logger.error(e);
                e.printStackTrace();
            }
            JSONObject resjson = new JSONObject();
            resjson.put("group", getTarget(body));

            sendImage(resjson, randomImgRes);
        } else {
            logger.info("image source 3");
            sendSeTuH(body);
        }
    }

    private void sendSeTuH(JSONObject body){
        String randomImgRes = getRandomImageFromRemote2();
        try{
            randomImgRes = DownloadURLFile.downloadImgByNet(randomImgRes, setuFormNetR18, String.valueOf(System.currentTimeMillis()) + ".jpg");
        } catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }
        JSONObject resjson = new JSONObject();
        resjson.put("group", getTarget(body));

        sendImage(resjson, randomImgRes);
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
        Pattern pattern = Pattern.compile("src=\"https://img1\\.picloli-1\\.xyz.*?\\.(jpg|png)");
//        Pattern pattern = Pattern.compile("url: \"https://img1\\.picloli-1\\.xyz.*?\\.jpg");
        Matcher matcher = pattern.matcher(randomImgRes);
        if (matcher.find()) {
            randomImgRes = matcher.group();
        }
        randomImgRes = randomImgRes.replaceAll("src=\"","");
//        randomImgRes = randomImgRes.replaceAll("url: \"","");
        return randomImgRes;
    }

    @Override
    public synchronized void sendImage(JSONObject targetJson, String imageUrl){
        ImageId imageId = imageIdMapper.getImageIdByPass(imageUrl);
        JSONArray array = new JSONArray();
        array.add(imageUrl);
        targetJson.put("urls", array);
        if(imageId == null){
            imageId = getImageId(imageUrl, imageId);
        }
        if(imageId != null){
//            targetJson.put("imageId", imageId.getImageid());
            targetJson.put("messageChain", createMessageImageChain(imageId.getImageid()));
            miraiMessageSenderService.sendMessage("GroupMessage", targetJson);
            return;
        }
        String res = miraiMessageSenderService.sendImageMessage(targetJson);
        if (imageId == null) {
            imageId = new ImageId();
            imageId.setPath(imageUrl);
            imageId.setImageid(JSONArray.parseArray(res).getString(0));
            imageIdMapper.insertImageId(imageId);
        }
    }

    private ImageId getImageId(String imagePath, ImageId imageId) {
        if(imageId != null && StringUtils.isNotBlank(imageId.getImageid())){
            return imageId;
        }
        String uploadres = miraiMessageSenderService.uploadImage(imagePath);
        JSONObject jsonres = JSONObject.parseObject(uploadres);
        if(jsonres.containsKey("imageId")){
            imageId = new ImageId();
            imageId.setPath(imagePath);
            imageId.setImageid(jsonres.getString("imageId"));
            imageIdMapper.insertImageId(imageId);
            return imageId;
        }
        return null;
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
