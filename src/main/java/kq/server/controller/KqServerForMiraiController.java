package kq.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import kq.server.bean.ImageId;
import kq.server.bean.Message;
import kq.server.bean.User;
import kq.server.config.Configuation;
import kq.server.mapper.ImageIdMapper;
import kq.server.mapper.UserMapper;
import kq.server.service.*;
import kq.server.threads.MiraiSender;
import kq.server.util.DownloadURLFile;
import kq.server.util.MiraiMessageUtil;
import kq.server.util.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class KqServerForMiraiController {
    private static Logger logger = Logger.getLogger(KqServerForMiraiController.class);
    @Autowired
    MiraiMessageHandlerService miraiMessageHandlerService;

    @ResponseBody
    @RequestMapping(value="/miraiEventsReceice", method= RequestMethod.POST)
    public String eventsReceice(@RequestBody JSONObject body){
        logger.info("Mirai上报消息 "+ body.toJSONString());

        long me = Configuation.getMe();
        if(!body.containsKey("sender") || body.getJSONObject("sender").getLong("id") == me){
            return "skip deal";
        }

        // 存放至线程变量中，方便在其它地方获取
        MiraiMessageUtil.miraiMessageThreadLocal.set(body);

        return miraiMessageHandlerService.doDeal(body);

    }

}
