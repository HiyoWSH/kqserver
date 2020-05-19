package kq.server.service;

import kq.server.bean.User;

public interface NormalService {
    String getNormalResStr(User user, String command);

    String getTuLingRes(String command) throws Exception;
    String doDealMenu();
}
