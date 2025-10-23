package com.example.chat_webflux.integration;

import redis.embedded.RedisServer;

/**
 * Embedded Redis Server
 * 전체 테스트 클래스가 캐싱 대상에 상관없이 공유하기 위해 Singleton 사용
 */
public class EmbeddedRedisSingleton {

    private static RedisServer redisServer;

    public static synchronized void start() {
        if (redisServer == null) {
            redisServer = new RedisServer(6379);
            redisServer.start();
        }
    }

    public static synchronized void stop() {
        if (redisServer != null) {
            redisServer.stop();
            redisServer = null;
        }
    }
}