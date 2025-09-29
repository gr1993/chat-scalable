import React, { useState, useEffect } from 'react';
import styled from 'styled-components';

import type { IMessage } from '@stomp/stompjs';

import FlexContainer from '@/components/common/FlexContainer';
import PrimaryButton from '@/components/common/PrimaryButton';
import ChatRoom from '@/components/ChatRoom';
import ChatRoomCreateModal from '@/components/ChatRoomCreateModal';

import { useAppStore } from '@/store/useAppStore';
import { useChatStore } from '@/store/useChatStore';
import { useNavigate } from 'react-router-dom';
import { handleApiResponse } from '@/api/apiUtils';
import type { ChatRoomInfo } from '@/api/types';
import { getRoomList, createRoom } from '@/api/chatRoom';
import { useChatSubscribe } from '@/hooks/useChatSubscribe';

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

  // 채팅방 생성 구독
  useChatSubscribe("/topic/rooms", (message: IMessage) => {
    const payload: ChatRoomInfo = JSON.parse(message.body);
    setRoomList((prev) => [...(prev ?? []), payload]);
  });
  
  useEffect(() => {
    setHeaderInfo(true, "채팅방 목록");

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