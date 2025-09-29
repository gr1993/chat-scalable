# chat-react

React를 사용해 간단한 채팅 기반 애플리케이션을 구현했으며, 기능 시연을 목적으로 제작했다.  

<pre><code>```
  [src]
    └── api : REST API 호출 로직
    └── assets : 앱이 사용하는 정적 리소스
    └── common : 공통 모듈 정의
    └── components : 페이지에 사용되는 컴포넌트 정의
    └── hooks : 커스텀 훅 정의
    └── layout : 각 페이지에서 공통적으로 쓰이는 레이아웃
    └── pages : URL 단위로 제작되는 페이지
    └── store : 전역 상태 관리 스토어
``` </code></pre>


### 프론트 기술
* Vite 7.0.5
* typescript
* react 19.1.0
* react-router-dom
* styled-components
    * 디자인 출처 : https://boseuleeee.tistory.com/51
* Zustand : 전역 상태 관리
* WebSocket : sockjs-client, @stomp/stompjs

### styled-components
styled-components를 사용한 가장 큰 이유는 컴포넌트 간 CSS 충돌을 방지할 수 있기 때문이다. 모든 스타일은  
고유한 해시 네임으로 컴파일되어, 다른 컴포넌트와 충돌하지 않는다. 또한 클래스명을 따로 지정할 필요가 없어  
네이밍에 대한 고민도 줄어든다. 스타일과 로직이 하나의 파일에 함께 있어 유지보수가 쉬우며, 각 스타일이 해당  
컴포넌트에만 종속되기 때문에 코드의 응집력이 높아진다.  

### Zustand
이전에 사용해봤던 Redux로 전역 상태 관리를 고려해보았지만, 현재 프로젝트는 성능 테스트를 하기 위해 아주  
간소화된 채팅 어플리케이션 기능만을 제공한다. Redux는 전역 상태 관리의 패턴을 강력하게 정의하고, 액션  
리듀서, 디스패치 등과 같이 상태 변화의 흐름을 명확하게 만들기 위한 구조가 있다. 그래서 이 구조는 규모가  
큰 프로젝트에 적합하다고 한다. 반면 Zustand는 훨씬 더 간단하고 상태를 설정하고 가져오는 것만으로 쉽게  
전역 상태 관리를 할 수 있다. 작은 프로젝트에 적합하므로 Zustand를 사용하여 전역 상태 관리를 구현하였다.  