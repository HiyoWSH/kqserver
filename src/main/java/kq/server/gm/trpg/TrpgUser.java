package kq.server.gm.trpg;

import kq.server.bean.User;

import java.util.LinkedHashMap;
import java.util.Map;

public class TrpgUser extends User {
    // 道具列表
    private Map<String, Integer> items = new LinkedHashMap<String, Integer>();
    // 状态列表
    private Map<String, Integer> status = new LinkedHashMap<String, Integer>();

    public boolean haveItems(String... names) {
        for (String name:names){
            if(!items.containsKey(name) || items.get(name) == 0){
                return false;
            }
        }
        return true;
    }

    public void getItems(String name, int count){
        if(!items.containsKey(name)){
            items.put(name, 0);
        }
        items.put(name, items.get(name) + count);
    }

    public boolean costItems(String name, int count){
        if(!items.containsKey(name)){
            items.put(name, 0);
            return false;
        }
        if(items.get(name) < count){
            return false;
        }
        items.put(name, items.get(name) - count);
        return true;
    }
}
