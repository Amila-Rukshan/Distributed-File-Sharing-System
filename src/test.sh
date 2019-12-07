#!/bin/sh
nc -u 127.0.1.1 55555
echo -n "0009 ECHO"

port=57000
#echo -n "0009 ECHO" | nc -u 127.0.1.1 57000
#for i in $(seq 0 3)
#do
#  for j in $(seq 0 2)
#  do
#    echo -n "0009 ECHO" | nc -u 127.0.1.1 $port
#    port=$((port+1));
#    sleep 1
#  done
#done