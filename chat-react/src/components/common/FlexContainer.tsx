import styled from 'styled-components';

// $로 시작하는 props를 자동으로 DOM 요소로 전달하지 않음
interface FlexContainerProps {
  $justifyContent?: string;
  $alignItems?: string;
  $flexDirection?: string;
}

const FlexContainer = styled.div<FlexContainerProps>`
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: ${({ $justifyContent }) => $justifyContent || 'center'};
  align-items: ${({ $alignItems }) => $alignItems || 'center'};
  flex-direction: ${({ $flexDirection }) => $flexDirection || 'row'};
  overflow-y: auto;
`;

export default FlexContainer;