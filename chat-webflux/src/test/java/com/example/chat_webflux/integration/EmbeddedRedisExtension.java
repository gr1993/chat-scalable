package com.example.chat_webflux.integration;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EmbeddedRedisExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        EmbeddedRedisSingleton.start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
    }
}