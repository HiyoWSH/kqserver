package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import kq.server.bean.Message;
import kq.server.bean.Tuling;
import kq.server.bean.User;
import kq.server.mapper.TulingMapper;
import kq.server.service.NormalService;
import kq.server.util.DealRegexLib;
import kq.server.util.MessageUtil;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NormalServiceImpl implements NormalService {

    @Autowired
    TulingMapper tulingMapper;

    private static Logger logger = Logger.getLogger(NormalServiceImpl.class);
    @Override
    public String getNormalResStr(User user, String command) {
        if(command.contains("夸我") || command.contains("爱我")  || command.contains("喜欢我")){
            String chp = getChp();
            if(chp != null){
                return chp;
            } else {
                return doDealNormal(command);
            }
        } else if(command.contains("色图")){
            // TUDO pro
            return doDealNormal(command);
        } else {
            return doDealNormal(command);
        }
    }


    /**
     * 未定义命令
     * @param command
     * @return
     */
    private String doDealNormal(String command) {
        if(doDealRegex(command) != null){
            return doDealRegex(command);
        }
        if(RandomUtil.getNextInt(100) >= 95){
            return "@栗小栗 并回复\"帮助\"可以查询栗小栗的使用方式哦";
        }
        try{
            return getTuLingRes(command);
        } catch (Exception e) {
            logger.warn(e);
        }

        String chp = getChp();
        if(chp != null){
            return chp;
        } else {
            return "喵喵喵";
        }
    }

    /**
     * 图灵机器人的回复
     * @return
     * @param command
     */
    @Override
    public String getTuLingRes(String command) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        String tulingUrl = "http://openapi.tuling123.com/openapi/api/v2";
        String apikey = "1d855315c79c475f81cf2e2d7dcb946a";
        String userId = "kqserverlixiaoli";
        String param = String.format("{\"reqType\":%d,\"perception\": {\"inputText\": {\"text\": \"%s\"}},\"userInfo\": {\"apiKey\": \"%s\",\"userId\": \"%s\"}}"
                , 0, command, apikey, userId);
        logger.info(String.format("send param = %s", param));

        String res = post(tulingUrl, param, MediaType.parseMediaType("application/json;charset=UTF-8"));
        logger.info("POSTTULING" + res);
        JSONObject json = JSONObject.parseObject(res);
        if(json.containsKey("results")) {
            Configuration conf = Configuration.defaultConfiguration();
            conf.addOptions(Option.ALWAYS_RETURN_LIST);
            String path =
                    new StringBuilder("$..results[*].values.text")
                            .toString();
            ReadContext ctx = JsonPath.parse(json);
            net.minidev.json.JSONArray arr = ctx.read(path);
            for(int i=0;i<arr.size();i++){
                if (i > 0) {
                    stringBuilder.append("\n");
                } else {
                }
                stringBuilder.append(arr.get(i));
            }
            tulingMapper.insertTuling(new Tuling(command, stringBuilder.toString()));
        } else {
            throw new Exception("图灵调用失败");
        }
        return stringBuilder.toString();
    }

    /**
     * 正则匹配
     * @param command
     * @return
     */
    private String doDealRegex(String command) {
        String regexRes = DealRegexLib.getRes(command);
        if(StringUtils.isNotBlank(regexRes)){
            return regexRes;
        }
        return null;
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
            logger.error(e);
            return null;
        }
    }

    private String post(String url, String param, MediaType mediaType){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(mediaType);
        HttpEntity requestEntity=new HttpEntity(param, requestHeaders);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(url, requestEntity, String.class);
    }


    /**
     * 菜单
     * @return
     */
    @Override
    public String doDealMenu() {
        return
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
                        "  TRPG MODE2 (体验版)\n" +
                        "色图指令：\n" +
                        "  想多了不存在的\n" +
                        "吐槽：\n" +
                        "  小心栗小栗吐槽你哦（已删）\n" +
                        "爱你❤：\n" +
                        "  栗小栗❤你哦\n" +
                        "其它指令：\n" +
                        "  你猜喵\n" +
                        "(系统重构中部分指令可能失效)";
    }
}
