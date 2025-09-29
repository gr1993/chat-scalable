#!/bin/bash

FAILED_NODE_ID=$1
FAILED_NODE_HOST=$2
FAILED_NODE_PORT=$3
FAILED_NODE_DIR=$4
NEW_MASTER_NODE_ID=$5
NEW_MASTER_HOST=$6

REPMGR=/usr/bin/repmgr

echo "---- Failover handler ----"
echo "Failed node id   : $FAILED_NODE_ID"
echo "Failed host      : $FAILED_NODE_HOST"
echo "New master id    : $NEW_MASTER_NODE_ID"
echo "New master host  : $NEW_MASTER_HOST"
echo "--------------------------"

# 만약 master가 죽었고 standby가 master로 승격된다면
if [ $FAILED_NODE_ID = 0 ]; then
    echo "Primary ($FAILED_NODE_HOST) is down. Promoting standby ($NEW_MASTER_HOST)..."

    # repmgr을 이용해 standby 승격 (pg_ctl promote 포함)
    ssh root@$NEW_MASTER_HOST "su - postgres -c '$REPMGR -f /etc/repmgr/repmgr.conf standby promote'"
fi

exit 0