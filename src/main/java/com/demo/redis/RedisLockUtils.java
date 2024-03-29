package com.demo.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @param
 * @author YiHaoXing
 * @description 通过redis实现分布式锁工具类
 * @date 18:02 2019/6/30
 * @return
 **/
@Component
@Slf4j
public class RedisLockUtils {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * @author YiHaoXing
     * @description
     * @date 16:53 2019/9/4
     * @param [lockKey, expireTime, timeUnit]
     * @return void
     **/
    public void getLock(String lockKey,int expireTime, TimeUnit timeUnit){
        RLock lock = redissonClient.getLock(lockKey);
        log.info("Thread:{}正在获取锁...",Thread.currentThread().getId());
        //拿不到锁线程会一直阻塞.直到拿到锁
        lock.lock(expireTime,timeUnit);
    }

    /**
     * @param [lockKey, value, waitTime, expireTime]
     * @return boolean
     * @author YiHaoXing
     * @description 可重入锁.默认
     * @date 18:16 2019/6/30
     **/
    public boolean getReentrantLock(String lockKey, int waitTime, int expireTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockKey);
        log.info("Thread:{}正在获取锁...",Thread.currentThread().getId());
        //拿不到锁会等待waitTime,如果过了waitTime依然没有拿到锁,则获取锁失败.
        return lock.tryLock(waitTime, expireTime, timeUnit);
    }

    /**
     * @param [lockKey, waitTime, expireTime, timeUnit, threadId]
     * @return boolean
     * @author YiHaoXing
     * @description 可重入锁(异步执行)
     * @date 18:33 2019/6/30
     **/
    public boolean getAsyncReentrantLock(String lockKey, int expireTime, TimeUnit timeUnit, Long threadId) {
        RLock lock = redissonClient.getLock(lockKey);
        RFuture<Void> rFuture;
        if (Optional.ofNullable(threadId).isPresent()) {
            rFuture = lock.lockAsync(expireTime, timeUnit, threadId);
            return rFuture.isSuccess();
        } else {
            rFuture = lock.lockAsync(expireTime, timeUnit);
        }
        return null == rFuture ? false : rFuture.isSuccess();
    }

    /**
     * @author YiHaoXing
     * @description 公平锁
     * @date 18:47 2019/6/30
     * @param [lockKey, waitTime, expireTime, timeUnit]
     * @return boolean
     **/
    public boolean getFairLock(String lockKey, int waitTime, int expireTime, TimeUnit timeUnit) throws InterruptedException {
        RLock fairLock = redissonClient.getFairLock(lockKey);
        return fairLock.tryLock(waitTime, expireTime, timeUnit);
    }

    /**
     * @author YiHaoXing
     * @description 公平锁(异步执行)
     * @date 18:48 2019/6/30
     * @param [lockKey, expireTime, timeUnit, threadId]
     * @return boolean
     **/
    public boolean getAsyncFairLock(String lockKey, int expireTime, TimeUnit timeUnit, Long threadId) {
        RLock fairLock = redissonClient.getFairLock(lockKey);
        RFuture<Void> rFuture;
        if (Optional.ofNullable(threadId).isPresent()) {
            rFuture = fairLock.lockAsync(expireTime, timeUnit, threadId);
            return rFuture.isSuccess();
        } else {
            rFuture = fairLock.lockAsync(expireTime, timeUnit);
        }
        return null == rFuture ? false : rFuture.isSuccess();
    }

    /**
     * @author YiHaoXing
     * @description 读写锁
     * @date 18:58 2019/6/30
     * @param [lockKey, waitTime, expireTime, timeUnit, threadId]
     * @return boolean
     **/
    public boolean getReadWriteLock(String lockKey, int waitTime,int expireTime, TimeUnit timeUnit, Long threadId) throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        //读锁
        //return readWriteLock.readLock().tryLock(waitTime, expireTime, timeUnit);

        //写锁
        return readWriteLock.writeLock().tryLock(waitTime, expireTime, timeUnit);
    }



    /**
     * @param [lockKey]
     * @return void
     * @author YiHaoXing
     * @description 释放锁
     * @date 18:25 2019/6/30
     **/
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        //如果释放锁的时候,redis的锁已经因为超时自动清除了.此时会报异常
        //java.lang.IllegalMonitorStateException: attempt to unlock lock, not locked by current thread by node id: 2ca6b4a4-60d1-424d-b131-9f139be12ff4 thread-id: 47
        lock.unlock();
    }

}