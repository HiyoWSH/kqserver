package kq.server.bean.rss;

import java.util.Map;

public abstract class RSS{
    String name;
    String rssurl;
    long targetGroup;
    Map<String, String> headers;
    String pattern;

    public RSS(String name, String rssurl, long targetGroup, Map<String, String> headers, String pattern) {
        this.name = name;
        this.rssurl = rssurl;
        this.targetGroup = targetGroup;
        this.headers = headers;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRssurl() {
        return rssurl;
    }

    public void setRssurl(String rssurl) {
        this.rssurl = rssurl;
    }

    public long getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(long targetGroup) {
        this.targetGroup = targetGroup;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public abstract String getRssRes();
}