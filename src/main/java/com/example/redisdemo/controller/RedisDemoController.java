package com.example.redisdemo.controller;

import com.example.redisdemo.bean.User;
import com.example.redisdemo.service.RedisDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
public class RedisDemoController {

    @Resource
    private RedisDemoService redisDemoService;

    /**
     * 新增用户
     *
     * @param user user
     */
    @RequestMapping("/addUser")
    public void addUser(@RequestBody User user) {
        redisDemoService.addUser(user);
    }


    /**
     * 获取用户
     *
     * @param userId userId
     * @return User
     */
    @RequestMapping("/getUser/{userId}")
    public User getUser(@PathVariable String userId) {
        return redisDemoService.getUser(userId);
    }

    /**
     * 测试分布式锁，删除库中物品个数
     *
     * @param goodId goodId
     */
    @RequestMapping("/deleteCount/{goodId}")
    public void deleteCount(@PathVariable String goodId) {
        redisDemoService.deleteCount(goodId);
    }

    /**
     * 使用注解测试分布式锁，删除库中物品个数
     *
     * @param goodId goodId
     */
    @RequestMapping("/deleteCountWithAnnotation/{goodId}")
    public void deleteCountWithAnnotation(@PathVariable String goodId) {
        redisDemoService.deleteCountWithAnnotation(goodId);
    }

    /**
     * 加入延迟队列
     */
    @RequestMapping("/addDelay")
    public void addDelay() {
        redisDemoService.addDelay();
    }
}
