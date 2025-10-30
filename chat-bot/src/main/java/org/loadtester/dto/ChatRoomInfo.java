package org.loadtester.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatRoomInfo {
    private Long roomId;
    private String roomName;
    private String lastId;
    private String lastMessage;
    private String lastDt;
}
