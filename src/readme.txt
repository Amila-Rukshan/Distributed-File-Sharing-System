we have simplified the run commands using shell/bash scripts
**following packages are needed to open multiple terminals as given
    1) nc       - to send udp messages from terminal
    2) xterm    - to open multiple peer nodes in multiple terminals

first go to "src" directory and run following scripts

./setup.sh                    // this will first start the Bootstrap Server (port 55555) and
                              // then start 12 peer nodes (57000 - 57011)

bash ./query.sh               // this will select three random unique ports in the range (57000 - 57011) and
                              // query each node with all the file names given
                              // need to press any key to continue to run the next search query

bash ./leave.sh               // this command will safely remove 2 random peer nodes



