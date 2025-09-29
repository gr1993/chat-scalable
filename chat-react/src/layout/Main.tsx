import React from 'react';
import styled from 'styled-components';

const StyledMain = styled.main`
  flex-grow: 1;
  min-height: 0;
`;

const Main: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <StyledMain>
      {children}
    </StyledMain>
  );
};

export default Main;