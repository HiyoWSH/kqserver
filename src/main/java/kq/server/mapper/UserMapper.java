package kq.server.mapper;

import kq.server.bean.User;

import java.util.List;

public interface UserMapper {
    public List<User> getUsers();
    public User getUser(long user_id);
    public void insertUser(User user);
    public void updateUser(User user);
}
