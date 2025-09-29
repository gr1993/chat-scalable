import React from 'react';
import styled from 'styled-components';

const StyledFooter = styled.footer`
  border-top: 1px solid #ddd;
  padding: 10px 0px;
`;

const Footer: React.FC = () => {
  return (
    <StyledFooter>
      <p style={{ fontSize: '12px', textAlign: 'center', color: '#aaa' }}>
        &copy; 2025 My App. All rights reserved.
      </p>
    </StyledFooter>
  );
};

export default Footer;