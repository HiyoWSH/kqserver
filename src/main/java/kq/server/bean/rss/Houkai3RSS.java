package kq.server.bean.rss;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.util.Map;

public class Houkai3RSS extends RSS {

    private static Logger logger = Logger.getLogger(Houkai3RSS.class);

    public Houkai3RSS(String name, String rssurl, long targetGroup, Map<String, String> headers, String pattern) {
        super(name, rssurl, targetGroup, headers, pattern);
    }

    @Override
    public String getRssRes() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://rsshub.app.cdn.cloudflare.net/bilibili/user/dynamic/27534330")
                    .get()
                    .addHeader("host", "rsshub.app")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Postman-Token", "13cf2521-a969-4fd6-a96d-f4ec7e9392ae")
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            return "";
        }
    }
}
