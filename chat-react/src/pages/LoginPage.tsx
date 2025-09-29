import React, { useState, useEffect } from 'react';
import styled from 'styled-components';

import FlexContainer from '@/components/common/FlexContainer';
import RoundInput from '@/components/common/RoundInput';
import PrimaryButton from '@/components/common/PrimaryButton';

import { useUserStore } from '@/store/useUserStore';
import { useAppStore } from '@/store/useAppStore';
import { useNavigate } from 'react-router-dom';
import { handleApiResponse } from '@/api/apiUtils';
import { entryUser } from '@/api/user';


const LoginBox = styled.form`
  width: 380px;
  background-color: #ffffff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  margin: 0px 20px;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
`;

const LoginPage: React.FC = () => {
  const [inputId, setInputId] = useState('');
  const { login } = useUserStore();
  const { setHeaderInfo } = useAppStore();
  const navigate = useNavigate();

  useEffect(() => {
    setHeaderInfo(false, "로그인");
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputId(e.target.value);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputId) {
      alert('아이디를 입력하세요.');
      return;
    }

    // 사용자 입장 API
    handleApiResponse(
      entryUser(inputId),
      () => {
        login(inputId);
        navigate('/room');
      }
    );
  };

  return (
    <FlexContainer>
      <LoginBox onSubmit={handleSubmit}>
        <RoundInput 
          type="text" 
          placeholder="아이디를 입력하세요" 
          value={inputId}
          onChange={handleChange}
        />
        <PrimaryButton type="submit">입장</PrimaryButton>
      </LoginBox>
    </FlexContainer>
  );
};

export default LoginPage;