import React from 'react';
import styled from 'styled-components';

import Header from './Header';
import Main from './Main';
import Footer from './Footer';

const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
  width: 400px;
  height: 600px;
  background-color: white;
`;

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <StyledDiv>
      <Header />
      <Main>{children}</Main>
      <Footer />
    </StyledDiv>
  );
};

export default Layout;