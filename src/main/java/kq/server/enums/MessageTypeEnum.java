package kq.server.enums;

public enum MessageTypeEnum {

    PRIVATE("private", "私聊消息"),
    GROUP("group","群消息"),
    DISCUSS("discuss","讨论组消息");

    private String type;
    private String description;

    MessageTypeEnum(String type, String description){
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static MessageTypeEnum getMessageTypeEnum(String type){
        for(MessageTypeEnum messageTypeEnum:MessageTypeEnum.values()){
            if(messageTypeEnum.type.equals(type)){
                return messageTypeEnum;
            }
        }
        return null;
    }
}
