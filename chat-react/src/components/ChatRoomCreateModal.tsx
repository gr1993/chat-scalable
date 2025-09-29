import React, { useState } from 'react';
import styled from 'styled-components';

interface Props {
  onClose: () => void;
  onCreate: (roomName: string) => void;
}

const Overlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: #f2f4f7;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
`;

const ModalWrapper = styled.div`
  width: 400px;
  height: 600px;
  background-color: white;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  padding: 30px 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
`;

const Title = styled.h2`
  font-size: 20px;
  margin-bottom: 30px;
  text-align: center;
`;

const Input = styled.input`
  font-size: 16px;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 10px;
  margin-bottom: 20px;
  outline: none;

  &:focus {
    border-color: #007aff;
  }
`;

const ButtonGroup = styled.div`
  margin-top: auto;
  display: flex;
  justify-content: space-between;
`;

const Button = styled.button<{ primary?: boolean }>`
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  background-color: ${props => (props.primary ? '#007aff' : '#ddd')};
  color: ${props => (props.primary ? 'white' : '#333')};
  margin-left: ${props => (props.primary ? '10px' : '0')};
  cursor: pointer;

  &:hover {
    opacity: 0.9;
  }
`;

const ChatRoomCreateModal: React.FC<Props> = ({ onClose, onCreate }) => {
  const [roomName, setRoomName] = useState('');

  const handleCreate = () => {
    if (roomName.trim()) {
      onCreate(roomName.trim());
    }
  };

  return (
    <Overlay>
      <ModalWrapper>
        <Title>채팅방 만들기</Title>
        <Input
          type="text"
          placeholder="채팅방 이름을 입력하세요"
          value={roomName}
          onChange={(e) => setRoomName(e.target.value)}
        />
        <ButtonGroup>
          <Button onClick={onClose}>취소</Button>
          <Button primary onClick={handleCreate}>방 생성</Button>
        </ButtonGroup>
      </ModalWrapper>
    </Overlay>
  );
};

export default ChatRoomCreateModal;