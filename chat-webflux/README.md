# chat-webflux

[이전 프로젝트 웹 서버](https://github.com/gr1993/chat-service/blob/main/chat-mvc/README.md)처럼 빌드했던 방식이 거의 유사하고, 리소스 제한을 똑같이 적용할 것이다.  
이번에는 다중 서버(3대)를 구동할 예정이며 앞단에 Nginx를 배치하여 리버스 프록시를 통한 로드밸런싱을 적용할 예정이다.  


### 백엔드 기술
* Spring Boot 3.5.5 (JDK 21)
* Spring Webflux 6.2.10
* r2dbc
  * r2dbc-postgresql
  * r2dbc-h2 : 테스트 전용 R2DBC 드라이버
* redis
  * redis-reactive
  * redisson : 분산 락 사용할 Redis Client
  * embedded-redis
* kafka
  * reactor-kafka
  * spring-kafka-test : @EmbeddedKafka 사용
* spring-boot-starter-actuator : 모니터링에 필요하며 Micrometer를 포함한 모듈
* micrometer-registry-prometheus : MeterRegistry 프로메테우스 구현체


### 도커 환경 구성
이 프로젝트는 jdk21 기반의 Dockerfile을 사용하여 Gradle로 빌드된다. (alpine 이미지로는 Embedded Redis 실행 불가)  
프로젝트 루트에 있는 docker-compose.yml 파일을 통해 빌드된 이미지를 실행할 수 있으며, 리소스가 제한된 환경에서  
서버를 구동하고 테스트할 수 있도록 구성되어 있다.

#### 컨테이너 실행 명령어
```shell
# 빌드 명령어(이번에는 같은 이미지를 3개 컨테이너가 사용하므로 빌드를 별도로 수행)
docker build -t myrepo/chat-server:1.0.0 .

# 실행 명령어
docker-compose up -d

# 재실행 명령어
# 명령어 파이프 (linux=&&, Window cmd=&, Window PowerShell=;)
docker-compose down && docker-compose up -d
```