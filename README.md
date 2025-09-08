# chat-scalable
확장성과 내결함성을 갖춘 WebFlux 기반 채팅 웹 서버 구현(Redis, Kafka 사용)

<pre><code>```
[chat-react]
    └── STOMP 없이 구현한 간단한 채팅 클라이언트 

[chat-bot]
    └── STOMP 제거 버전의 시뮬레이션용 부하 테스트 봇

[chat-webflux]
    └── 비상태(Stateless) 구조의 Spring WebFlux + Netty 기반 채팅 서버

[chat-infra] 
    └── MySQL, Redis, Kafka 등 인프라 구성 스크립트
```</code></pre>


## 프로젝트 개요
[이전 프로젝트](https://github.com/gr1993/chat-service)에서는 MVC와 WebFlux 기반의 채팅 웹 서버를 구현하고, 해당 서버에 대해 성능 테스트를   
진행하였다. 당시 구현한 웹 서버는 메모리에 상태를 유지하는 모놀리식 아키텍처로 설계되었었다.  
이번 프로젝트에서는 확장성을 고려한 비상태(Stateless) WebFlux 기반의 채팅 서버를 구축할 예정이다.  
서버의 상태는 RDBMS와 Kafka에 저장하며, 이를 통해 더 높은 처리량을 지원하는 시스템을 구현하고자 한다.  
이러한 구성은 확장성과 내결함성(Fault Tolerance)에 대한 이해를 높이기 위한 목적도 포함하고 있다.  

### Redis 사용 이유
이번에는 STOMP 기술을 제거하고 WebSocket만을 사용하며, Redis의 Pub/Sub 기능을 활용하여 채팅 서버의
메시지를 각 클라이언트가 실시간으로 공유할 수 있도록 구현할 예정이다. Redis는 메모리 기반으로 동작하기 때문에
실시간 통신에 유리하다. 따라서 웹 서버는 Redis에 기본적인 정보를 저장하고, Kafka를 통해 RDBMS까지 데이터를
전달하는 데이터 파이프라인을 구축할 계획이다.



## 아키텍처 구성도
