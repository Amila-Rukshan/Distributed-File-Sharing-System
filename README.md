### 4.1 Register/Unregister With Bootstrap Server

#### Register Request message – used to register with the BS
<code>length REG IP_address port_no username</code>
Ex: 0036 REG 129.82.123.45 5001 1234abcd

#### Register Response message – BS will send the following message
<code>length REGOK no_nodes IP_1 port_1 IP_2 port_2</code>
Ex: 0051 REGOK 2 129.82.123.45 5001 64.12.123.190 34001
    
    0       – request is successful, no nodes in the system
    1 or 2  – request is successful, 1 or 2 nodes' contacts will be returned
    9999    – failed, there is some error in the command
    9998    – failed, already registered to you, unregister first
    9997    – failed, registered to another user, try a different IP and port
    9996    – failed, can’t register. BS full

#### Unregister Request message – used to unregister from the BS
<code>length UNREG IP_address port_no username</code>
Ex: 0028 UNREG 64.12.123.190 432 username

#### Unregister Response message – BS will send the following message
<code>length UNROK value</code>
Ex: 0012 UNROK 0

    value   – Indicate success or failure.
    0       – successful
    9999    – error while unregistering. IP and port may not be in the registry or command is incorrect.

### 4.2 Join Distributed System

#### Join Request message – used to indicate presence of new node to other nodes that is found from BS
<code>length JOIN IP_address port_no</code>
Ex: 0027 JOIN 64.12.123.190 432

#### Join Response message
<code>length JOINOK value</code>
    value   – Indicate success or failure
    0       – successful
    9999    – error while adding new node to routing table

### 4.3 Leave Distributed System

#### Leave Request message – used to indicate this node is leaving the distributed system
<code>length LEAVE IP_address port_no</code>
Ex: 0028 LEAVE 64.12.123.190 432

#### Response message
<code>length LEAVEOK value</code>
Ex: 0014 LEAVEOK 0
    value   – Indicate success or failure
    0       – successful 
    9999    – error while removing new node from routing table

### 4.4 Search for a File Name

#### Search Request message – Used to locate a key in the network
<code>length SER IP port file_name hops</code>
Ex: Suppose we are searching for Lord of the rings, 0047 SER 129.82.62.142 5070 "Lord of the rings"
    length      – Length of the entire message including 4 characters used to indicate the length. In xxxx format.
    SER         – Locate a file with this name.
    IP          – IP address of the node that is searching for the file. May be useful depending your design.
    port        – port number of the node that is searching for the file. May be useful depending your design.
    file_name   – File name being searched.
    hops        – A hop count. May be of use for cost calculations (optional).

#### Response message – Response to query originator when a file is found.
<code>length SEROK no_files IP port hops filename1 filename2 ... ... </code>
Ex: Suppose we are searching for string baby. So it will return, 0114 SEROK 3 129.82.128.1 2301 baby_go_home.mp3 baby_come_back.mp3 baby.mpeg

    length      – Length of the entire message including 4 characters used to indicate the length. In xxxx format.
    SEROK       – Sends the result for search. The node that sends this message is the one that actually stored the (key, value) pair, i.e., node that index the file information.
    no_files    – Number of results returned
            ≥ 1     – Successful
            0       – no matching results. Searched key is not in key table
            9999    – failure due to node unreachable
            9998    – some other error.
    IP          – IP address of the node having (stored) the file.
    port        – Port number of the node having (stored) the file.
    hops        – Hops required to find the file(s).
    filename    – Actual name of the file.

#### 4.5 Error Message
<code>length ERROR</code>
Ex: 0010 ERROR

    length  – Length of the entire message including 4 characters used to indicate the length. In xxxx format.
    ERROR   – Generic error message, to indicate that a given command is not understood. For storing and searching files/keys this should be send to the initiator of the message.






















