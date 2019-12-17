# saving stats of each node
port=57000
for i in $(seq 0 11)
do
  echo -n "0013 SAVE_LOG" | nc -u 127.0.1.1 $port &
  port=$((port+1))
done


