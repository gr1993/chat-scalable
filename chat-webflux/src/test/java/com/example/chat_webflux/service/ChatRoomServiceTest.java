package com.example.chat_webflux.service;

import com.example.chat_webflux.common.ChatRoomManager;
import com.example.chat_webflux.dto.ChatRoomInfo;
import com.example.chat_webflux.dto.SendMessageInfo;
import com.example.chat_webflux.dto.WsJsonMessage;
import com.example.chat_webflux.entity.ChatRoom;
import com.example.chat_webflux.entity.ChatUser;
import com.example.chat_webflux.entity.OutboxEvent;
import com.example.chat_webflux.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomManager chatRoomManager;

    @Mock
    private ObjectMapper objectMapper;


    @Test
    void getRoomList_성공() {
        // given
        List<ChatRoom> mockRoomList = List.of(
                new ChatRoom("park"),
                new ChatRoom("kang")
        );
        when(chatRoomRepository.findAll())
                .thenReturn(Flux.fromIterable(mockRoomList));

        // when
        List<ChatRoomInfo> roomList = chatRoomService.getRoomList().block();

        // then
        assertFalse(roomList.isEmpty());
        assertEquals(2, roomList.size());
    }

    @Test
    void createRoom_성공() throws Exception {
        // given
        String roomName = "park";
        ChatRoom newChatRoom = new ChatRoom(roomName);

        when(chatRoomRepository.save(any(ChatRoom.class)))
                .thenReturn(Mono.just(newChatRoom));

        when(outboxEventService.saveOutboxEvent(
                any(String.class),
                ArgumentMatchers.<Map<String, Object>>any())
        ).thenReturn(Mono.just(new OutboxEvent(UUID.randomUUID())));

        Sinks.Many<String> mockSink = mock(Sinks.Many.class);
        when(chatRoomManager.getChatServerSinks())
                .thenReturn(mockSink);

        when(objectMapper.writeValueAsString(any(WsJsonMessage.class)))
                .thenReturn("{}");

        // when
        chatRoomService.createRoom(roomName).block();

        // then
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(mockSink).tryEmitNext(anyString());
    }

    @Test
    void enterRoom_성공() {
        // given
        ChatRoom chatRoom = new ChatRoom("park");
        ChatUser chatUser = new ChatUser("kang");
        when(chatMessageService.sendMessageToRoom(any(SendMessageInfo.class), eq(true)))
                .thenReturn(Mono.empty());

        // when
        chatRoomService.enterRoom(chatRoom.getId(), chatUser.getId()).block();

        // then
        verify(chatMessageService).sendMessageToRoom(any(SendMessageInfo.class), eq(true));
    }

    @Test
    void exitRoom_성공() {
        // given
        ChatRoom chatRoom = new ChatRoom("park");
        ChatUser chatUser = new ChatUser("kang");
        when(chatMessageService.sendMessageToRoom(any(SendMessageInfo.class), eq(true)))
                .thenReturn(Mono.empty());

        // when
        chatRoomService.exitRoom(chatRoom.getId(), chatUser.getId()).block();

        // then
        verify(chatMessageService).sendMessageToRoom(any(SendMessageInfo.class), eq(true));
    }
}
