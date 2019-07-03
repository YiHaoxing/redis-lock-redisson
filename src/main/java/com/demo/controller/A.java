package com.demo.controller;

import com.demo.redis.RedisLockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.demo.controller.A
 * @description TODO
 * @date 2019/6/30 22:45
 */
@RestController
public class A {
    @Autowired
    private RedisLockUtils redisLockUtils;

    @GetMapping("/A")
    public String test(){
        try {
            redisLockUtils.getReentrantLock("KEY-A",1000,30000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            redisLockUtils.unlock("KEY-A");
        }
        return "Hello";
    }
}
