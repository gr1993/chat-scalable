package com.example.chat_webflux.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table
@NoArgsConstructor
public class OutboxEvent implements Persistable<UUID> {

    @Id
    private UUID eventId;

    /**
     * 기본 동작이 @Id 필드가 null이면 INSERT를 null이 아니면 UPDATE가 동작하도록 설정됨
     * 그러나 OutboxEvent 엔티티는 eventId를 직접 어플리케이션에서 지정해야 해서 isNew 플래그가 필요함
     * 필수 : implements Persistable<UUID>
     */
    @Transient
    private boolean isNew = false;

    private String eventType;
    private String eventVersion;
    private LocalDateTime createDt;
    private String payload;
    private String status;
    private LocalDateTime sentAt;

    public OutboxEvent(UUID eventId) {
        this.eventId = eventId;
        this.isNew = true;
    }

    @Override
    public UUID getId() {
        return eventId;
    }
}
