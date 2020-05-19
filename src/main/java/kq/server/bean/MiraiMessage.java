package kq.server.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class MiraiMessage {
    // 为主动产生的消息标记id
    private static int createId = 627900000;

    public MiraiMessage(){
        this.id = createId();
    }

    public MiraiMessage(int target, String type, JSONArray messageChain) {
        this.id = createId();
        this.target = target;
        this.type = type;
        this.messageChain = messageChain;
        JSONObject json = new JSONObject();
        json.put("target", target);
        json.put("type", type);
        json.put("messageChain", messageChain);
        this.body = json;
    }

    public MiraiMessage(String type, JSONObject sender, JSONArray messageChain, JSONObject body) {
        this.type = type;
        this.sender = sender;
        this.messageChain = messageChain;
        this.body = body;
    }

    private static int createId(){
        return MiraiMessage.createId++;
    }

    /**
     * 消息id， 运行期间唯一
     */
    private int id;
    /**
     * 消息目标， 主动产生的消息会有
     */
    private int target;
    /**
     * 消息类型， 暂时只支持私聊和群聊
     */
    private String type;
    /**
     * 消息来源， 上报的消息会有
     */
    private JSONObject sender;
    /**
     * 消息主体
     */
    private JSONArray messageChain;
    /**
     * 消息本体
     */
    private JSONObject body;
    /**
     * 主动发送的消息产生的返回值
     */
    private String msgres;

    private long atTarget;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getSender() {
        return sender;
    }

    public void setSender(JSONObject sender) {
        this.sender = sender;
    }

    public JSONArray getMessageChain() {
        return messageChain;
    }

    public void setMessageChain(JSONArray messageChain) {
        this.messageChain = messageChain;
    }

    public JSONObject getBody() {
        return body;
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

    public String getMsgres() {
        return msgres;
    }

    public void setMsgres(String msgres) {
        this.msgres = msgres;
    }

    public long getAtTarget() {
        return atTarget;
    }

    public void setAtTarget(long atTarget) {
        this.atTarget = atTarget;
    }
}

class MiraiSender{
    long id;
    String memberName;
    String permission;
    Group group;
    String nickname;
    String remark;
}

class Group{
    long id;
    String name;
    String permission;
}