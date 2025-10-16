import React, { useState, useEffect } from 'react';
import styled from 'styled-components';

import { getWebSocket } from '@/common/socketClient';

import FlexContainer from '@/components/common/FlexContainer';
import PrimaryButton from '@/components/common/PrimaryButton';
import ChatRoom from '@/components/ChatRoom';
import ChatRoomCreateModal from '@/components/ChatRoomCreateModal';

import { useAppStore } from '@/store/useAppStore';
import { useChatStore } from '@/store/useChatStore';
import { useStrictEffect } from '@/hooks/useStrictEffect';
import { useNavigate } from 'react-router-dom';
import { handleApiResponse } from '@/api/apiUtils';
import type { ChatRoomInfo } from '@/api/types';
import { getRoomList, createRoom } from '@/api/chatRoom';

const RoomBox = styled.div`
  flex: 1;
  overflow-y: auto;
  width: 100%;
`;

const ButtonBox = styled.div`
  width: 100%;
  padding: 15px;
  display: flex;
  flex-direction: column;
`;

const ChatRooms: React.FC = () => {
  const [roomList, setRoomList] = useState<ChatRoomInfo[] | null>([]);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  const { setHeaderInfo } = useAppStore();
  const { setCurrentRoom } = useChatStore();
  const navigate = useNavigate();

  useStrictEffect(() => {
    setHeaderInfo(true, "채팅방 목록");

    const ws = getWebSocket();
      if (ws) {
        // 채팅방 생성 메시지 구독 요청
        ws.send(JSON.stringify({
          type: "SUBSCRIBE",
          destination: "/topic/rooms",
        }));

        // 구독된 메시지 처리
        ws.onmessage = (event) => {
          const wsMsg = JSON.parse(event.data);
          if(wsMsg.type == 'ROOM_MESSAGE') {
            const data = wsMsg.data;
            const payload: ChatRoomInfo = {
              roomId: data.roomId,
              roomName: data.roomName,
            };
            setRoomList((prev) => [...(prev ?? []), payload]);
          }
        };
      }

    // 전체 채팅방 목록 조회 API
    handleApiResponse(
      getRoomList(),
      (data) => {
        setRoomList(data);
      }
    );

    return () => {};
  }, []);

  const createChatRoom = (roomName: string) => {
    // 전체 채팅방 생성 API
    handleApiResponse(
      createRoom(roomName),
      () => {
        setIsModalOpen(false);
      }
    );
  }

  const handleRoomClick = (room: ChatRoomInfo) => {
    setCurrentRoom({ id: room.roomId, name: room.roomName });
    navigate("/chat");
  }

  const handleCreateRoomClick = () => {
    setIsModalOpen(true);
  }

  return (
    <FlexContainer $flexDirection="column" $justifyContent="flex-start">
      <RoomBox>
        {roomList?.map((room) => (
          <ChatRoom key={room.roomId} room={room} onClick={handleRoomClick} />
        ))}
      </RoomBox>
      <ButtonBox>
        <PrimaryButton type="button" onClick={handleCreateRoomClick} >방 생성</PrimaryButton>
      </ButtonBox>
      {isModalOpen && (
        <ChatRoomCreateModal
          onClose={() => setIsModalOpen(false)}
          onCreate={createChatRoom}
        />
      )}
    </FlexContainer>
  );
};

export default ChatRooms;