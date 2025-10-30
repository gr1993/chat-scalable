package org.loadtester.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.loadtester.client.HttpClient;
import org.loadtester.dto.ApiResponse;
import org.loadtester.dto.ChatRoomInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

public class ChatService {
    private final String restApiBaseUrl;
    private final HttpHeaders headers;

    private HttpClient httpClient = new HttpClient();

    public ChatService(String restApiBaseUrl) {
        this.restApiBaseUrl = restApiBaseUrl;

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * 사용자 로그인(서버에 입장)
     */
    public void login(String userId) {
        httpClient.post(restApiBaseUrl + "/api/user/enter/" + userId, null);
    }

    /**
     * 존재하는 채팅방 ID 조회(채팅방이 없으면 생성 후 반환)
     */
    public Long getExistRoomId() {
        ChatRoomInfo roomInfo = getFirstRoomInfo();
        if (roomInfo == null) {
            createRoom("부하테스트방");
        }
        return getFirstRoomInfo().getRoomId();
    }

    /**
     * 채팅방에 입장
     */
    public void enterRoom(Long roomId, String userId, String sessionId) {
        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        sessionHeaders.add("X-Session-Id", sessionId);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("userId", userId);
        httpClient.post(restApiBaseUrl + "/api/room/" + roomId + "/enter", sessionHeaders, formData);
    }

    /**
     * 채팅방에서 퇴장
     */
    public void exitRoom(Long roomId, String userId) {
        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("userId", userId);
        httpClient.post(restApiBaseUrl + "/api/room/" + roomId + "/exit", sessionHeaders, formData);
    }

    private ChatRoomInfo getFirstRoomInfo() {
        ApiResponse<List<ChatRoomInfo>> response = httpClient.fetchAndParse(
                restApiBaseUrl + "/api/room",
                null,
                new TypeReference<ApiResponse<List<ChatRoomInfo>>>() {}
        );

        List<ChatRoomInfo> roomInfoList = response.getData();
        return roomInfoList.stream()
                .findAny()
                .orElse(null);
    }

    private void createRoom(String roomName) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", roomName);
        httpClient.post(restApiBaseUrl + "/api/room", headers, formData);
    }
}
