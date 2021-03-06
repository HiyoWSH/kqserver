package kq.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.Message;
import kq.server.service.StoryService;
import kq.server.service.UserService;
import kq.server.util.FileOperate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static kq.server.util.MessageUtil.*;
import static kq.server.util.RandomUtil.*;

@Service
public class StoryServiceImpl implements StoryService {

    private static List bookIndex;
    @Autowired
    private UserService userService;

    @Override
    public String doDealStory(String command) {
        StringBuilder resbuilder = new StringBuilder();
        try {
            String param = command.replaceAll(".故事", "").trim();
            String targetBook = "";
            int index = 0;

            if (StringUtils.isNotBlank(param)) {
                List<String> searchRes = new ArrayList<>();
                index = getStorysByParam(searchRes, bookIndex, param.split(" "));
                if (searchRes.size() > 0) {
                    int r = getNextInt(searchRes.size());
                    targetBook = searchRes.get(r);
                }
            } else {
                List nbList = bookIndex;
                while (true && nbList.size() > 0) {
                    int r = getNextInt(nbList.size());
                    Object o = nbList.get(r);
                    if (o instanceof String) {
                        targetBook = (String) o;
                    } else if (o instanceof List) {
                        nbList = (List) o;
                        continue;
                    } else {

                    }
                    break;
                }
            }

            if (StringUtils.isNotBlank(targetBook)) {

                List<String> values = FileOperate.readFileToStringList(targetBook, "gb2312");
                int startIndex = index;
                if (startIndex == 0 || startIndex >= values.size()){
                    startIndex = getNextInt(values.size());
                }
                int endIndex = startIndex + 30;
                StringBuilder builder = new StringBuilder();
                for (int i = startIndex; i < endIndex || i < values.size() - 1; i++) {
                    builder.append(values.get(i));
                }
                resbuilder.append("\n").append(new File(targetBook).getName().replaceAll(".txt", ""));
                resbuilder.append("\n").append(builder.substring(0, Math.min(990, builder.length())).toString() + "...");

                logger.info("Get story : " + targetBook + " start index " + startIndex);
            } else {
                resbuilder.append("\n").append("栗小栗好像忘了怎么讲故事了呢，诶嘿~");
            }

            return resbuilder.toString();
        } catch (Exception e) {
            logger.error(e);
            resbuilder.append("\n").append("栗小栗好像忘了怎么讲故事了呢，诶嘿~");
            return resbuilder.toString();
        }
    }

    private int getStorysByParam(List<String> searchRes, List searchTarget, String... param) {
        int storyIndex = 0;
        for (Object o:searchTarget){
            if(o instanceof String){
                boolean matched = true;
                for (String p:param){
                    if(StringUtils.isNotBlank(p)){
                        if(p.matches("[0-9]+")){
                            storyIndex = Integer.parseInt(p);
                        } else {
                            matched = ((String) o).contains(p) && matched;
                        }
                    }
                }
                if(matched){
                    searchRes.add((String) o);
                }
            } else if (o instanceof List) {
                getStorysByParam(searchRes, (List) o, param);
            } else {

            }
        }
        return storyIndex;
    }

    /**
     * 初始化书籍索引
     */
    @Override
    public void initStoryCache(){
        long start = System.currentTimeMillis();
        String StoryDir = "D:\\txtbook\\";
        File f = new File(StoryDir);
        bookIndex = getStorys(f);
        logger.info("initStoryCache end " + (System.currentTimeMillis()-start) + "ms");
    }

    private List getStorys(File fileDir){
        List list = new ArrayList();
        for(File cfile:fileDir.listFiles()){
            if(cfile.isDirectory()){
                list.add(getStorys(cfile));
            } else if (cfile.getName().endsWith("txt")){
                list.add(cfile.getPath());
            }
        }
        return list;
    }

    public static void main(String argv[]){
        File f = new File("D:\\input.txt");
        System.out.println(f.getName());
        System.out.println("1965".matches("[0-9]*"));
    }
}
