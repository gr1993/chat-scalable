# chat-infra

PostgreSQL, Redis, Kafka 클러스터를 도커 컨테이너로 구동할 수 있는 디렉토리이다. docker-compose 명령어를 통해  
쉽게 채팅 어플리케이션을 운영할 수 있는 환경을 구성할 수 있다.  

아래는 Kafka 클러스터가 구축되고 난 후 파티션 수를 지정하기 위해 직접 토픽 생성 명령어를 실행하였다.

#### Kafka 토픽 생성
```shell
docker exec -it kafka1 kafka-topics --create --topic chat.message.created --bootstrap-server kafka1:9091 --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --create --topic chat.message.notification --bootstrap-server kafka1:9091 --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --create --topic chat.room.created --bootstrap-server kafka1:9091 --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --create --topic chat.room.notification --bootstrap-server kafka1:9091 --partitions 3 --replication-factor 3
docker exec -it kafka1 kafka-topics --create --topic chat.user.created --bootstrap-server kafka1:9091 --partitions 3 --replication-factor 3
```