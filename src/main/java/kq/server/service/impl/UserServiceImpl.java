package kq.server.service.impl;

import kq.server.bean.User;
import kq.server.mapper.UserMapper;
import kq.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public boolean costCoins(User user, int count) {
        if(user == null){
            return false;
        }
        if(user.getCoins() >= count){
            user.setCoins(user.getCoins() - count);
            userMapper.updateUser(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean costCoins(int user_id, int count) {
        return costCoins(userMapper.getUser(user_id), count);
    }

    @Override
    public boolean getCoins(User user, int count) {
        if(user == null){
            return false;
        }
        user.setCoins(user.getCoins() + count);
        userMapper.updateUser(user);
        return true;
    }

    @Override
    public boolean getCoins(int user_id, int count) {
        return getCoins(userMapper.getUser(user_id), count);
    }
}
