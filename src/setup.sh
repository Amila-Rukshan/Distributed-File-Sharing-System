#remove previous log file
rm ./query_log_*.txt

#start bootstrap server
kill $(lsof -t -i:55555);
sleep 1
xterm -e javac BootstrapServer.java; java BootstrapServer &
sleep 2

#start and connect peers
port=57000
for i in $(seq 0 3)
do
  for j in $(seq 0 2)
  do
    NEW_UUID=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 32 | head -n 1);
    xterm -geometry "74x10+$((8+450*j))+$((28+165*i))" -e "javac Peer.java; java Peer $port $NEW_UUID" &
    port=$((port+1));
    sleep 1
  done
done











