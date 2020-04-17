package kq.server.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.enums.MessageTypeEnum;
import org.apache.log4j.Logger;
import org.junit.Test;

public class MessageUtil {

    public static Logger logger = Logger.getLogger(MessageUtil.class);

    public static JSONObject addAt(JSONObject json, int targetId){
        JSONObject data = new JSONObject();
        data.put("qq", targetId);

        JSONObject at = new JSONObject();
        at.put("type", "at");
        at.put("data", data);

        if(!json.containsKey("message")){
            json.put("message", new JSONArray());
        }
        json.getJSONArray("message").add(at);

        return json;
    }

    public static JSONObject addJsonMessage(JSONObject json, String msg){
        JSONObject msgdata = new JSONObject();
        msgdata.put("text", msg);

        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("data", msgdata);

        json.getJSONArray("message").add(message);
        return json;
    }

    public static JSONObject addJsonMessageWithEnter(JSONObject json, String msg){
        JSONObject msgdata = new JSONObject();
        msgdata.put("text", "\n" + msg);

        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("data", msgdata);

        json.getJSONArray("message").add(message);
        return json;
    }

    public static JSONObject addJsonMessageWithEnterFirst(JSONObject json, String msg){
        JSONObject msgdata = new JSONObject();
        msgdata.put("text", "\n" + msg);

        JSONObject message = new JSONObject();
        message.put("type", "text");
        message.put("data", msgdata);

        json.getJSONArray("message").add(0, message);
        return json;
    }

    public static JSONObject getResBase(Message message) {
        if(MessageTypeEnum.GROUP == message.getMessage_type()){
            return getResBaseGroup(message);
        } else {
            return getResBasePrivate(message);
        }
    }

    public static JSONObject getResBaseGroup(Message message) {
        JSONObject resjson = new JSONObject();
        resjson.put("group_id",message.getGroup_id());
        resjson.put("message", new JSONArray());
        addAt(resjson, message.getUser_id());
        return resjson;
    }

    public static JSONObject getResBasePrivate(Message message) {
        JSONObject resjson = new JSONObject();
        resjson.put("user_id",message.getUser_id());
        resjson.put("message", new JSONArray());
        return resjson;
    }

    public static JSONObject getNormalRes(Message message) {
        return getNormalRes(message, "喵喵喵？");
    }

    public static JSONObject getNormalRes(Message message, String str) {
        JSONObject resjson = getResBase(message);
        addJsonMessage(resjson, str);
        logger.info("send message, " + resjson.toJSONString());
        return resjson;
    }

    @Test
    public void messageDealTest(){
        JSONObject body =
                JSONObject.parseObject("{\n" +
                        "\t\"raw_message\": \"666\",\n" +
                        "\t\"sender\": {\n" +
                        "\t\t\"age\": 18,\n" +
                        "\t\t\"nickname\": \"Kuribana Satou\",\n" +
                        "\t\t\"sex\": \"male\",\n" +
                        "\t\t\"user_id\": 463832436\n" +
                        "\t},\n" +
                        "\t\"sub_type\": \"friend\",\n" +
                        "\t\"user_id\": 463832436,\n" +
                        "\t\"self_id\": 627985299,\n" +
                        "\t\"message_id\": 6258,\n" +
                        "\t\"message_type\": \"private\",\n" +
                        "\t\"post_type\": \"message\",\n" +
                        "\t\"time\": 1585549779,\n" +
                        "\t\"message\": \"666\",\n" +
                        "\t\"font\": 167558952\n" +
                        "}");
//        JSONObject.parseObject("{\"raw_message\":\"转发转发\"," +
//                "\"self_id\":627985299,\"message_id\":6002,\"message_type\":\"group\"," +
//                "\"message\":\"转发转发\",\"group_id\":532559793,\"sender\":{\"age\":17,\"area\":\"泽西岛\",\"card\":\"神代代代代\",\"level\":\"活跃\",\"nickname\":\"iS＇Kc゛\",\"role\":\"admin\",\"sex\":\"male\",\"title\":\"\",\"user_id\":937211908},\"sub_type\":\"normal\",\"user_id\":937211908,\"post_type\":\"message\",\"time\":1585533189,\"font\":57071152} ");

        Message message = new Message(body, true);
        String command = message.getCommand();
//        System.out.printf("command:%s \n", command);
        JSONObject resjson = getNormalRes(message);
//        JSONObject resjson = getChouqianRes(message);
        System.out.println(resjson.toJSONString());
    }
}
