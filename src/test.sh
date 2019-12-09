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


echo -n "0018 TRIGGER_LEAVE" | nc -u 127.0.1.1 57000

#echo -n "0024 SER 127.0.1.1 57000 "" 3" | nc -u 127.0.1.1 57000
#length SER IP port file_name hops

echo -n "0047 SER 127.0.1.1 57000 \"Twilight\" 3" | nc -u 127.0.1.1 57000
echo -n "0036 SER 127.0.1.1 57000 \"windows\" 2" | nc -u 127.0.1.1 57003



echo -n "0036 DOWNLOAD_TRIGGER 127.0.1.1 57000 \"filename\" 2" | nc -u 127.0.1.1 55555








