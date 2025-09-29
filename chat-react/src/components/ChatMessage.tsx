import type { ChatMessageInfo } from '@/api/types';
import styled from 'styled-components';

const StyledChat = styled.div`
  display: flex;
  flex-direction: column;
  margin: 10px 0;
`;

const NameTag = styled.span`
  font-size: 12px;
  color: #999;
  margin-bottom: 3px;
`;

const Bubble = styled.p`
  padding: 10px 15px;
  border-radius: 20px;
  max-width: 70%;
  word-wrap: break-word;
  font-size: 15px;
`;

const Left = styled(StyledChat)`
  align-items: flex-start;

  ${Bubble} {
    background-color: #eaeaea;
    color: #333;
  }
`;

const Right = styled(StyledChat)`
  align-items: flex-end;

  ${Bubble} {
    background-color: #4A90E2;
    color: white;
  }
`;

const SystemMessageWrapper = styled.div`
  display: flex;
  justify-content: center;
  margin: 20px 0;
`;

const SystemMessage = styled.div`
  background-color: rgba(0, 0, 0, 0.05);
  padding: 7px 12px;
  border-radius: 15px;
  font-size: 13px;
  color: #666;
`;

type ChatMessageProps = {
  message: ChatMessageInfo;
};

const ChatMessage: React.FC<ChatMessageProps> = ({ message }) => {
  if (message.type === 'system') {
    return (
      <SystemMessageWrapper key={message.messageId}>
        <SystemMessage>{message.message}</SystemMessage>
      </SystemMessageWrapper>
    );
  }

  if (message.position === 'left') {
    return (
      <Left key={message.messageId}>
        <NameTag>{message.senderId}</NameTag>
        <Bubble>{message.message}</Bubble>
      </Left>
    );
  } else {
    return (
      <Right key={message.messageId}>
        <NameTag>{message.senderId}</NameTag>
        <Bubble>{message.message}</Bubble>
      </Right>
    );
  }
};

export default ChatMessage;