#!/usr/bin/env bash

PAYLOAD=$1
SLEEP=$2

if [[ -z "$SLEEP" ]]
then
    SLEEP=0
fi


if [[ -z "$PAYLOAD" ]]
then
    PAYLOAD="zaccoding"
fi

generate_data()
{
    cat <<EOF
    {
        "requestId": "1",
        "payload" : "$PAYLOAD",
        "sleep" : $SLEEP
    }
EOF
}

#echo $(generate_data)

curl -H "Content-Type: application/json" \
         --data "$(generate_data)" \
         -X POST localhost:8080/rpc