package com.example.chat_webflux.entity;

import com.example.chat_webflux.kafka.KafkaEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table
@NoArgsConstructor
public class ChatUser implements Persistable<String>, KafkaEvent {

    @Id
    private String id;

    /**
     * 기본 동작이 @Id 필드가 null이면 INSERT를 null이 아니면 UPDATE가 동작하도록 설정됨
     * 그러나 ChatUser 엔티티는 id를 직접 어플리케이션에서 지정해야 해서 isNew 플래그가 필요함
     * 필수 : implements Persistable<String>
     */
    @Transient
    private boolean isNew = false;

    private LocalDateTime createDt;

    public ChatUser(String id) {
        this.id = id;
        this.isNew = true;
        this.createDt = LocalDateTime.now();
    }
}
