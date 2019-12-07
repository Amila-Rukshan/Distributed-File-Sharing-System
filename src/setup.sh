
#start bootstrap server
#kill $(lsof -t -i:55555);
xterm -e javac BootstrapServer.java; java BootstrapServer &

#start and connect peers
for i in $(seq 57001 57010)
do
  NEW_UUID=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 32 | head -n 1)
  xterm -geometry +100+200 -e top "javac Peer.java; java Peer $i $NEW_UUID" &

done

