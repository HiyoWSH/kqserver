package kq.server.mapper;

import kq.server.bean.rss.RSSBean;

public interface RSSMapper {
    RSSBean getRss(String name);
    void insertRss(RSSBean rssBean);
    void updateRss(RSSBean rssBean);
}
