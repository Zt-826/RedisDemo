package com.example.redisdemo.mapper;

import com.example.redisdemo.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RedisDemoMapper {
    int addUser(@Param("user") User user);

    User getUser(@Param("userId") String userId);

    void deleteCount(@Param("goodId") String goodId);
}
