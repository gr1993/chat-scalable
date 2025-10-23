package com.example.chat_webflux;

import com.example.chat_webflux.integration.EmbeddedRedisExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(EmbeddedRedisExtension.class)
@SpringBootTest
class ChatWebfluxApplicationTests {

	@Test
	void contextLoads() {
	}

}
