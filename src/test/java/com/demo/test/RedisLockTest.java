package com.demo.test;

import com.demo.redis.RedisLockUtils;
import jodd.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;

/**
 * @author YiHaoXing
 * @version 1.0.0
 * @className com.demo.test.RedisLockTets
 * @description TODO
 * @date 2019/6/30 21:52
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisLockTest {

    @Autowired
    private RedisLockUtils redisLockUtils;

    @Test
    public void testReentrantLock() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().get();
        ExecutorService executorService = new ThreadPoolExecutor(
                10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());

        for (int i = 0; i < 10; i++) {
            int num = i;
            executorService.execute(() -> {
                Thread.currentThread().setName("Thread" + num);

                try {
                    boolean lock = redisLockUtils.getReentrantLock(String.valueOf(num), 1000, 20000, TimeUnit.MILLISECONDS);
                    if (lock) {
                        System.out.println(Thread.currentThread().getName()+" do something");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    redisLockUtils.unlock(String.valueOf(num));
                }

            });
        }
        executorService.shutdown();
    }

    @Test
    public void testReentrantLock2() {
        try{
            boolean lock = redisLockUtils.getReentrantLock("KAY_A", 1000, 20000, TimeUnit.MILLISECONDS);
            if (lock) {
                System.out.println("do something");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            redisLockUtils.unlock("KAY_A");
        }

    }

}
