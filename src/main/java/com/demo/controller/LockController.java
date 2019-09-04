package com.demo.controller;

import com.demo.redis.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.demo.controller.A
 * @description 测试Redis锁
 * @date 2019/6/30 22:45
 */
@RestController
@Slf4j
public class LockController {
    @Autowired
    private RedisLockUtils redisLockUtils;

    /**
     * 依次访问
     * http://localhost:8080/thread1/T
     * http://localhost:8080/thread2/T
     * 首先thread1或先获取到锁.然后thread2的请求到达.
     * 此时thread1在睡眠.因此还没有释放锁.thread2未能获取到锁.
     * thread2获取锁时设置了5S等待时间.等待5S后,thread1已经释放了锁.此时thread2可以成功获取到锁.
     * 这就是tryLock()方法.
     */
    @GetMapping("/thread1/{key}")
    public String thread1(@PathVariable String key){
        try {
            //等待时间1S.缓存过期时间30S
            boolean lock = redisLockUtils.getReentrantLock(key, 1, 30, TimeUnit.SECONDS);
            if(lock){
                //do something.
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            }else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            redisLockUtils.unlock(key);
            log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        }
        return "thread1 over";
    }
    @GetMapping("/thread2/{key}")
    public String thread2(@PathVariable String key){
        try {
            //等待时间5S.缓存过期时间30S
            boolean lock = redisLockUtils.getReentrantLock(key, 5, 30, TimeUnit.SECONDS);
            if(lock){
                //do something.
                log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
                Thread.sleep(5000);
            }else {
                log.info("获取锁失败,Thread:{}",Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放锁
            redisLockUtils.unlock(key);
            log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        }
        return "thread2 over";
    }


    /**
     * 依次访问
     * http://localhost:8080/thread3/F
     * http://localhost:8080/thread4/F
     * thread3获取到锁后.然后thread4请求到达.
     * thread3会睡眠10S,则thread4会一直阻塞直到成功获取锁.
     * 这就是lock()方法.获取锁不可中断,线程会阻塞直到获取锁.
     */
    @GetMapping("/thread3/{key}")
    public String g1(@PathVariable String key){
        redisLockUtils.getLock(key, 30, TimeUnit.SECONDS);
        log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //释放锁
        redisLockUtils.unlock(key);
        log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        return "thread3 over";
    }
    @GetMapping("/thread4/{key}")
    public String g2(@PathVariable String key){
        redisLockUtils.getLock(key, 30, TimeUnit.SECONDS);
        log.info("获取锁成功,Thread:{}",Thread.currentThread().getId());
        //释放锁
        redisLockUtils.unlock(key);
        log.info("释放锁,Thread:{}",Thread.currentThread().getId());
        return "thread4 over";
    }

}
