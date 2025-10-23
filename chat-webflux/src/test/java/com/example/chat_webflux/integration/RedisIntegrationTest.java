package com.example.chat_webflux.integration;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Embedded Redis Server
 * 테스트 실행 시 자체 Redis를 띄움
 */
@Import(LocalRedisConfig.class)
@SpringBootTest
public class RedisIntegrationTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Test
    void saveRedisString_성공() {
        // given
        String address = "myHome";

        // when
        reactiveRedisTemplate.opsForValue().set("address", address).block();

        // then
        String savedAddress = reactiveRedisTemplate.opsForValue().get("address").block();
        assertEquals(address, savedAddress);
    }

    @Test
    void testDistributedLock() throws Exception {
        // given
        String lockKey = "testLock";
        int threadCount = 10;
        AtomicInteger sharedCounter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = false;
            try {
                // 최대 2초 대기, 락 획득 시 5초 후 자동 해제
                locked = lock.tryLock(2, 5, java.util.concurrent.TimeUnit.SECONDS);
                if (locked) {
                    // 락 획득 성공 시 공유 변수 1 증가
                    int current = sharedCounter.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " acquired lock. Counter: " + current);
                    // 작업 시뮬레이션 (잠시 대기)
                    Thread.sleep(100);
                } else {
                    System.out.println(Thread.currentThread().getName() + " failed to acquire lock.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
                latch.countDown();
            }
        };

        // when (여러 스레드 동시에 실행)
        for (int i = 0; i < threadCount; i++) {
            new Thread(task, "Thread-" + i).start();
        }
        // 모든 스레드 작업 종료 대기
        latch.await();

        // then
        // 공유 변수 값이 threadCount 와 같아야 락이 제대로 동작한 것
        assertEquals(threadCount, sharedCounter.get());
    }
}
