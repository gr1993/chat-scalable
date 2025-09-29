import React from 'react';
import styled from 'styled-components';

import { useAppStore } from '@/store/useAppStore';
import { useNavigate } from 'react-router-dom';

const StyledHeader = styled.header`
  background-color: #4A90E2;
  color: white;
  height: 60px;
  padding: 20px;
  font-size: 20px;
  font-weight: bold;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const BackButton = styled.button`
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
`;

const Title = styled.div`
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 20px;
  font-weight: bold;
`;

const Placeholder = styled.div`
  width: 40px;
  height: 40px;
`;

const Header: React.FC = () => {
  const { isBackButton, headerTitle } = useAppStore();
  const navigate = useNavigate();

  const handleBackClick = () => {
    navigate(-1);
  }

  return (
    <StyledHeader>
      {isBackButton ? (
        <BackButton onClick={handleBackClick}>←</BackButton>
      ) : (
        <Placeholder />
      )}
      
      <Title>{headerTitle}</Title>

      {/* 오른쪽 여백 맞춤용 빈 div */}
      <Placeholder />
    </StyledHeader>
  );
};

export default Header;