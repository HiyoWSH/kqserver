package kq.server.bean;

import com.alibaba.fastjson.JSONObject;
import kq.server.enums.MessageTypeEnum;
import kq.server.util.MessageUtil;

public class Message {

    private static String MYINFO = "[CQ:at,qq=627985299]";
    private static String MYAT = "@栗小栗";
    private static String MYINFOCONTEXT = "\\[CQ:at,qq=627985299\\]";

    private JSONObject body;
    private MessageTypeEnum message_type;
    private int user_id;
    private int group_id;
    private String sub_type;
    private String post_type;
    private String raw_message;
    private JSONObject resbody;
    private String targetUrl;

    public Message() {
    }

    void init(){
        this.message_type = MessageTypeEnum.getMessageTypeEnum(body.getString("message_type"));
        this.post_type = body.getString("post_type");
        this.user_id = body.getInteger("user_id");
        this.sub_type = body.getString("sub_type");
        if(this.message_type == MessageTypeEnum.GROUP){
            this.group_id = body.getInteger("group_id");
        }
        if(this.post_type.equals("message")){
            this.raw_message = body.getString("raw_message");
        }
    }

    public Message(JSONObject body, boolean init) {
        this.body = body;
        if(init){
            init();
        }
    }

    public JSONObject getBody() {
        return body;
    }

    public void setBody(JSONObject body) {
        this.body = body;
        init();
    }

    public MessageTypeEnum getMessage_type() {
        return message_type;
    }

    public void setMessage_type(MessageTypeEnum message_type) {
        this.message_type = message_type;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getSub_type() {
        return sub_type;
    }

    public void setSub_type(String sub_type) {
        this.sub_type = sub_type;
    }

    public String getPost_type() {
        return post_type;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
    }

    public String getRaw_message() {
        return raw_message;
    }

    public void setRaw_message(String raw_message) {
        this.raw_message = raw_message;
    }

    public JSONObject getResbody() {
        return resbody;
    }

    public void setResbody(JSONObject resbody) {
        this.resbody = resbody;
    }

    public void addJsonRes(String res){
        MessageUtil.addJsonMessage(resbody, res);
    }

    public void addJsonResWithEnter(String res){
        MessageUtil.addJsonMessageWithEnter(resbody, res);
    }

    public void addJsonResWithEnterFirst(String res){
        MessageUtil.addJsonMessageWithEnterFirst(resbody, res);
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public boolean needDeal() {
        switch (post_type){
            case "message":break;
            default:
                return false;
        }
        switch (message_type) {
            // 私聊
            case PRIVATE:
                return true;
            // 群
            case GROUP:
                if(!this.raw_message.contains(MYINFO) && !this.raw_message.contains(MYAT)){
                    return false;
                };
                break;
            //讨论组
            case DISCUSS:
            default:
                return false;
        }
        return true;
    }

    public String getCommand() {
        return this.raw_message.replaceAll(MYINFOCONTEXT, "").replaceAll(MYAT, "").trim();
    }

    public static void main(String[] arg){
        String t = "[CQ:at,qq=627985299]...";
        System.out.println(t.replaceAll(MYINFOCONTEXT, ""));
    }
}
