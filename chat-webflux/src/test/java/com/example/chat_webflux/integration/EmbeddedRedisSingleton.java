package com.example.chat_webflux.integration;

import redis.embedded.RedisServer;

/**
 * Embedded Redis Server
 * 전체 테스트 클래스가 캐싱 대상에 상관없이 공유하기 위해 Singleton 사용
 * @SpringBootTest만 붙은 테스트 클래스 간 전환 시에도, @MockBean처럼 컨텍스트에 영향을
 * 주는 경우 캐싱 대신 컨텍스트가 재로딩되기 때문에 싱글톤으로 관리하여 중복 실행 문제를 방지
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