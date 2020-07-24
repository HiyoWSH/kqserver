package kq.server.rss;

import com.alibaba.fastjson.JSONObject;
import kq.server.bean.rss.RSS;
import kq.server.bean.rss.RSSBean;
import kq.server.mapper.RSSMapper;
import kq.server.service.MiraiMessageSenderService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RSSpush extends Thread {

    private static Logger logger = Logger.getLogger(RSSpush.class);
    // RSS推送
    List<RSS> rssList = new ArrayList<>();
    RSSMapper rssMapper;
    MiraiMessageSenderService miraiMessageSenderService;


    public RSSpush() {
    }

    public RSSpush(RSSMapper rssMapper, MiraiMessageSenderService miraiMessageSenderService) {
        this.rssMapper = rssMapper;
        this.miraiMessageSenderService = miraiMessageSenderService;
    }

    public void addRss(RSS rss){
        rssList.add(rss);
    }

    @Override
    public void run(){
        while (true){
            try {
                sleep(2 * 60 * 1000L);

                logger.info("开始检查rss订阅");
                for (RSS rss:rssList){
                    String rssres = getRssRes(rss);
                    logger.info(rssres);
                    if(hasUpdate(rss, rssres)){
                        try {
                            dopush(rss, rssres);
                        } catch (Exception e){
                            logger.info(e);
                            continue;
                        }
                    }
                }
                logger.info("rss订阅检查结束");

                sleep(18 * 60 * 1000L);
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
    }

    public void dopush(RSS rss, String rssres) {
        try {

            if(rssres.length() > 500) {
                String pushBody = getRssPushBody(rssres);
                JSONObject pushjson = JSONObject.parseObject("{\n" +
                        "    \"target\": 1,\n" +
                        "    \"messageChain\": [\n" +
                        "        { \"type\": \"Plain\", \"text\": \"1\" }\n" +
                        "    ]\n" +
                        "}");
                pushjson.put("target", rss.getTargetGroup());
                pushjson.getJSONArray("messageChain").getJSONObject(0).put("text", pushBody);
//            JSONObject pushjson = JSONObject.parseObject(String.format("{\n" +
//                    "    \"target\": %d,\n" +
//                    "    \"messageChain\": [\n" +
//                    "        { \"type\": \"Text\", \"%s\" }\n" +
//                    "    ]\n" +
//                    "}", rss.getTargetGroup(), pushBody));
                miraiMessageSenderService.sendMessage("GroupMessage", pushjson);

                RSSBean rssBean = rssMapper.getRss(rss.getName());
                if (rssBean == null) {
                    rssBean = new RSSBean();
                    rssBean.setName(rss.getName());
                    rssBean.setUpdate_time(new Date());
                    rssMapper.insertRss(rssBean);
                } else {
                    rssBean.setUpdate_time(new Date());
                    rssMapper.updateRss(rssBean);
                }
            } else {
                logger.warn("rss 疑似过短" + rssres);
            }
        } catch (Exception e){
            logger.info(e);
        } finally {
            logger.info("dopush final");
        }

    }

    public boolean hasUpdate(RSS rss, String rssres) {
        RSSBean rssBean = rssMapper.getRss(rss.getName());
        if(rssBean == null){
            return true;
        } else if(rssBean.getUpdate_time().getTime() < getRssupdateTime(rssres)) {
            return true;
        }
        return false;
    }

    private long getRssupdateTime(String rssres) {
        rssres = rssres.substring(rssres.indexOf("<item>"), rssres.indexOf("</item>"));
        String pubDate = rssres.substring(rssres.indexOf("<pubDate>")+9, rssres.indexOf("</pubDate>"));
        logger.info("getRssupdateTime " + pubDate);
        return new Date(pubDate).getTime();
    }

    private String getRssPushBody(String rssres){
        rssres = rssres.substring(rssres.indexOf("<item>"), rssres.indexOf("</item>"));
        String body = rssres.substring(rssres.indexOf("<![CDATA[")+9, rssres.indexOf("]]>"));
        String link = rssres.substring(rssres.indexOf("<link>")+6, rssres.indexOf("</link>"));
        logger.info("getRssPushBody " + body + "\n" + link);
        return body + "\n" + link;
    }

    public String getRssRes(RSS rss) {
        return rss.getRssRes();
    }

    public static void main(String[] argv){
        String rssres = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss  xmlns:atom=\"http://www.w3.org/2005/Atom\" version=\"2.0\"\n" +
                "\n" +
                ">\n" +
                "    <channel>\n" +
                "        <title>\n" +
                "            <![CDATA[崩坏3第一偶像爱酱 的 bilibili 动态]]>\n" +
                "        </title>\n" +
                "        <link>https://space.bilibili.com/27534330/#/dynamic</link>\n" +
                "        <atom:link href=\"http://rsshub.app/bilibili/user/dynamic/27534330\" rel=\"self\" type=\"application/rss+xml\" />\n" +
                "        <description>\n" +
                "            <![CDATA[崩坏3第一偶像爱酱 的 bilibili 动态 - Made with love by RSSHub(https://github.com/DIYgod/RSSHub)]]>\n" +
                "        </description>\n" +
                "        <generator>RSSHub</generator>\n" +
                "        <webMaster>i@diygod.me (DIYgod)</webMaster>\n" +
                "        <language>zh-cn</language>\n" +
                "        <lastBuildDate>Mon, 01 Jun 2020 02:17:10 GMT</lastBuildDate>\n" +
                "        <ttl>60</ttl>\n" +
                "        <item>\n" +
                "            <title>\n" +
                "                <![CDATA[#崩坏3# 作战凭证第八期——「虚空回响」 开启！\n" +
                "空无之钥、仿犹大·血之拥抱、火刀·真田、古斯塔夫·克里姆特开放兑换~\n" +
                "本期作战凭证解锁「精英凭证」的舰长，...]]>\n" +
                "            </title>\n" +
                "            <description>\n" +
                "                <![CDATA[#崩坏3# 作战凭证第八期——「虚空回响」 开启！\n" +
                "空无之钥、仿犹大·血之拥抱、火刀·真田、古斯塔夫·克里姆特开放兑换~\n" +
                "本期作战凭证解锁「精英凭证」的舰长，可以立刻获得奖励：限定头像「空」！ <br><img src=\"https://i0.hdslb.com/bfs/album/82de22dd57d775cfd4c91a37daae5b880b68fe77.jpg\" referrerpolicy=\"no-referrer\">]]>\n" +
                "            </description>\n" +
                "            <pubDate>Mon, 01 Jun 2020 02:04:44 GMT</pubDate>\n" +
                "            <guid isPermaLink=\"false\">https://t.bilibili.com/395732634484184481</guid>\n" +
                "            <link>https://t.bilibili.com/395732634484184481</link>\n" +
                "        </item>\n" +
                "        <item>\n" +
                "            <title>\n" +
                "                <![CDATA[#崩坏3# #miHoYo旗舰店# ★清风联动纸巾第二弹贩售★\n" +
                "霁月清风下，萦绕指尖的温度，是女武神们独特的心意。\n" +
                "——「崩坏3x清风」特别联动第二弹！\n" +
                "6月1日零点，「...]]>\n" +
                "            </title>\n" +
                "            <description>\n" +
                "                <![CDATA[#崩坏3# #miHoYo旗舰店# ★清风联动纸巾第二弹贩售★\n" +
                "霁月清风下，萦绕指尖的温度，是女武神们独特的心意。\n" +
                "——「崩坏3x清风」特别联动第二弹！\n" +
                "6月1日零点，「崩坏3x清风」原木纯品抽纸联动版第二弹贩售即将开启！新增幽兰黛尔·辉骑士月魄、符华·云墨丹心以及八重樱·夜隐重霞款，快来PICK舰长最喜爱的女武神吧~<br><img src=\"https://i0.hdslb.com/bfs/album/2c62f1d7c2d0ba531e395ed5fc8ad997c78f7c95.jpg\" referrerpolicy=\"no-referrer\"><img src=\"https://i0.hdslb.com/bfs/album/cb4e53d041667ace19aed3aed745f2f947f7651e.jpg\" referrerpolicy=\"no-referrer\"><img src=\"https://i0.hdslb.com/bfs/album/74061338fc1a50624734395cc70a16fc0270ac12.jpg\" referrerpolicy=\"no-referrer\"><img src=\"https://i0.hdslb.com/bfs/album/a9ae77edaa645176aaa7e821b9db085e1b7a5851.jpg\" referrerpolicy=\"no-referrer\"><img src=\"https://i0.hdslb.com/bfs/album/003ffea2913c93108d6991bfa0c1218e1d84121b.jpg\" referrerpolicy=\"no-referrer\">]]>\n" +
                "            </description>\n" +
                "            <pubDate>Sun, 31 May 2020 11:57:01 GMT</pubDate>\n" +
                "            <guid isPermaLink=\"false\">https://t.bilibili.com/395514179556472897</guid>\n" +
                "            <link>https://t.bilibili.com/395514179556472897</link>\n" +
                "        </item>\n" +
                "    </channel>\n" +
                "</rss>".replaceAll("\\s*", "");
        rssres = rssres.substring(rssres.indexOf("<item>")+6, rssres.indexOf("</item>"));
        String body = rssres.substring(rssres.indexOf("<![CDATA[")+9, rssres.indexOf("]]>"));
        String link = rssres.substring(rssres.indexOf("<link>")+6, rssres.indexOf("</link>"));
        String pubDate = rssres.substring(rssres.indexOf("<pubDate>")+9, rssres.indexOf("</pubDate>"));
//        System.out.println(body);
//        System.out.println(link);
        System.out.println(new Date(pubDate).getTime());
    }

}

