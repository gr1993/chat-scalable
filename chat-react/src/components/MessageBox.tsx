import { useState } from 'react';
import styled from 'styled-components';

const MessageInput = styled.input``;
const SendInput = styled.button``;
const BoxContainer = styled.div`
  display: flex;
  padding: 15px 15px;
  width: 100%;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;

  ${MessageInput} {
    flex-grow: 1;
    padding: 12px 15px;
    border-radius: 25px;
    border: 1px solid #ddd;
    font-size: 16px;
    outline: none;
  }

  ${SendInput} {
    background-color: #4A90E2;
    border: none;
    padding: 12px 20px;
    border-radius: 25px;
    color: white;
    margin-left: 10px;
    cursor: pointer;
    font-size: 16px;

    &:hover {
      background-color: #357ABD;
    }
  }
`;

interface Props {
  onSend: (message: string) => void;
}

const MessageBox: React.FC<Props> = ({ onSend }) => {
  const [message, setMessage] = useState('');

  const handleSend = () => {
    if (!message.trim()) return;
    onSend(message.trim());
    setMessage('');
  };

  return (
    <BoxContainer>
      <MessageInput 
        type='text' 
        placeholder='메시지를 입력하세요...'
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && handleSend()}
      />
      <SendInput onClick={handleSend}>전송</SendInput>
    </BoxContainer>
  );
};

export default MessageBox;