package kq.server.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 正则语料库
 */
public class DealRegexLib {
    static Map<String, String[]> regexLibMap = new HashMap();

    public static void put(String regex, String... res){
        regexLibMap.put(regex, res);
    }

    static {
        put("歪比.*", "歪比歪比", "歪比巴卜");
        put("贴贴.*", "贴贴~", "蹭蹭~");
        put("抱抱.*", "抱抱", "抱~");
        put("rua.*", "rua!", "rua");
        put("RUA.*", "rua!", "rua");
    }

    public static String getRes(String input) {
        for(String regex : regexLibMap.keySet()){
            if(input.matches(regex)){
                return regexLibMap.get(regex)[RandomUtil.getNextInt(regexLibMap.get(regex).length)];
            }
        }
        return "";
    }

}
