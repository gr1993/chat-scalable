# chat-monitoring

[이전 프로젝트](https://github.com/gr1993/chat-service/blob/main/chat-monitoring/README.md)에서 Prometheus와 Grafana를 이용해서 지표를 수집하고 시각화하였다.  
이번에도 같은 도구들을 사용할 예정이며 Alertmanger는 구성하지 않을 예정이다. 필요 시 이전 프로젝트를 참조하면 된다.  
또한 Grafana 시각화 구성하는 방법도 이전 프로젝트에 정리해두었으니 참고하길 바란다.  


#### 이번 프로메테우스에 사용된 지표명
```
# CPU 사용률
system_cpu_usage
process_cpu_usage

# 메모리 사용량
jvm_memory_used_bytes{area="heap"}

# GC 횟수 / 최대 지연 시간
jvm_gc_pause_seconds_count
jvm_gc_pause_seconds_max

# REST API 관련 지표(방 입장 API)
http_server_requests_seconds_count{uri="/api/room/{roomId}/enter"}
# 방 입장 API 평균 응답시간이며 급격한 변화율을 감지하고 싶어서 1분 이내로 설정
rate(http_server_requests_seconds_sum{uri="/api/room/{roomId}/enter"}[1m]) / rate(http_server_requests_seconds_count{uri="/api/room/{roomId}/enter"}[1m])
# 최근 1분 동안 가장 오래걸린 응답시간
max_over_time(http_server_requests_seconds_max{uri="/api/room/{roomId}/enter"}[1m])

# 웹소켓 메시지 전송 지표(커스텀 지표)
# 어플리케이션에서 websocket_message_seconds 커스텀 지표 추가
# 누적 호출수
websocket_message_seconds_count
# TPS
rate(websocket_message_seconds_count[1m])
# 최근 1분 동안 가장 오래걸린 처리시간
max_over_time(websocket_message_seconds_max[1m])

# 동시 접속자 수(커스텀 지표)
# 웹소켓 연결 수
chat_app_active_connections

# 현재 쓰레드수(mvc와 webflux가 사용하는 쓰레드수 차이를 비교하기 위해 추가)
jvm_threads_live_threads
```