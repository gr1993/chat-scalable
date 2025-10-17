CREATE TABLE chat_user (
    id VARCHAR(20) PRIMARY KEY,
    create_dt TIMESTAMP NOT NULL
);

CREATE TABLE chat_room (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    create_dt TIMESTAMP NOT NULL
);

CREATE TABLE chat_message (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sender_id VARCHAR(50) NOT NULL,
    room_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    send_dt TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_room FOREIGN KEY (room_id) REFERENCES chat_room(id)
);


CREATE TABLE outbox_event (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    event_version VARCHAR(50) DEFAULT 'v1',
    create_dt TIMESTAMP NOT NULL DEFAULT NOW(),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING / SENT / FAILED
    sent_at TIMESTAMP NULL
);

CREATE INDEX idx_outbox_event_status_created
    ON outbox_event (status, create_dt);