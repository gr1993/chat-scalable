import type { ChatRoomInfo } from '@/api/types';
import styled from 'styled-components';
import defaultProfile from '../assets/default-profile.png';

const ChatRoomContainer = styled.div`
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  width: 100%;

  &:hover {
    background-color: #f9f9f9;
  }
`;

const ProfileImg = styled.img`
  width: 55px;
  height: 55px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 15px;
`;

const NameLast = styled.div``;
const Name = styled.div``;
const LastMessage = styled.div``;
const TimeBadge = styled.div``;
const Time = styled.span``;
const UnreadBadge = styled.span``;


const ChatInfo = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  width: 100%;

  ${NameLast} {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    flex-grow: 1;
  }

  ${Name} {
    font-weight: bold;
    font-size: 16px;
  }

  ${LastMessage} {
    color: #555;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  ${TimeBadge} {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    justify-content: center;
    min-width: 60px;  
    margin-left: auto;
    margin-right: 4px;
  }

  ${Time} {
    font-size: 12px;
    color: gray;
  }

  ${UnreadBadge} {
    background-color: #FF3B30;
    color: white;
    font-size: 12px;
    padding: 4px 7px;
    border-radius: 20px;
    margin-left: 5px;
  }
`;

type ChatRoomProps = {
  room: ChatRoomInfo;
  onClick?: (room: ChatRoomInfo) => void;
};

const ChatRoom: React.FC<ChatRoomProps> = ({ room, onClick }) => {
  return (
    <ChatRoomContainer onClick={() => onClick?.(room)}>
      <ProfileImg src={defaultProfile} alt="프로필 이미지" />
      <ChatInfo>
        <NameLast>
          <Name>{room.roomName}</Name>
          <LastMessage>{room.lastMessage ?? "메시지가 없습니다"}</LastMessage>
        </NameLast>
        <TimeBadge>
          <Time>{room.lastDt ?? ""}</Time>
        </TimeBadge>
      </ChatInfo>
    </ChatRoomContainer>
  );
};

export default ChatRoom;