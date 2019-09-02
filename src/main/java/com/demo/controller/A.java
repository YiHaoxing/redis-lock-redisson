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
            boolean lock = redisLockUtils.getReentrantLock("T", 1000, 60000, TimeUnit.MILLISECONDS);
            if(lock){
                //do something.
                System.out.println("获取锁成功");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            redisLockUtils.unlock("T");
        }
        return "Hello World";
    }
}
