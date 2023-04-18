package com.example.redisdemo.service;

import com.example.redisdemo.bean.User;
import com.example.redisdemo.mapper.RedisDemoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class RedisDemoService {
    @Resource
    private RedisDemoMapper mapper;

    /**
     * 新增用户
     *
     * @param user user
     */
    @Transactional(rollbackFor = Exception.class)
    public int addUser(User user) {
        return mapper.addUser(user);
    }

    /**
     * 返回用户
     *
     * @param userId userId
     * @return User
     */
    public User getUser(String userId) {
        return mapper.getUser(userId);
    }

    /**
     * 库存扣减
     *
     * @param goodId goodId
     */
    public void deleteCount(String goodId) {
        mapper.deleteCount(goodId);
    }
}
