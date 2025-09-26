FROM postgres:17

# repmgr 설치
RUN apt-get update && \
    apt-get install -y postgresql-17-repmgr && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 기본 위치 디렉터리 생성 및 권한 변경
RUN mkdir -p /etc/repmgr /var/log/repmgr && \
    chown postgres:postgres /etc/repmgr /var/log/repmgr