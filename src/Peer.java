/*
 *
 * compile and run : javac Peer.java; java Peer <port> <username>
 *
 *
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.security.MessageDigest;

public class Peer{

    public static List<String> assignedFiles;

    public static ServerSocket server = null;

    public static int ser_msg_count = 0;

    public static int fwd_msg_count = 0;

    public static int ans_msg_count = 0;

    static List<Node> peerTable;

    static String username;

    static int port;

    public static void main(String args[]){
        DatagramSocket sock = null;

        String s;

        peerTable = new ArrayList<Node>();

        assignedFiles = new ArrayList<>();


        String[] possilbeFiles = {"Adventures of Tintin", "Jack and Jill", "Glee", "The Vampire Diarie", "King Arthur",
                "Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight", "Windows 8", "Mission Impossible",
                "Turn Up The Music", "Super Mario", "American Pickers", "Microsoft Office 2010", "Happy Feet", "Modern Family",
                "American Idol", "Hacking for Dummies" };

        try{
            // port of the peer
            port = Integer.parseInt(args[0]);

            // username of the peer
            username = args[1];

            InetAddress ip = InetAddress.getLocalHost();
            InetAddress bsIP = InetAddress.getByName("127.0.1.1");
            int bsPort = 55555;

            // udp socket
            sock = new DatagramSocket(port);
            echo("Peer is listening on port : " + sock.getLocalPort());

            // tcp connection
            server = new ServerSocket(port);

            // file assigning
            possilbeFiles = shuffleArray(possilbeFiles);
            Random rand = new Random();
            int numFiles = 3 + rand.nextInt(3);

            echo(">> ASSIGNING FILES <<");
            for(int j = 0; j < numFiles; j++){
                assignedFiles.add(possilbeFiles[j]);
                echo(possilbeFiles[j]);
            }
            echo(">> FILES ASSIGNED <<");



            // try to register the node at the BS
            String msg = String.format(" REG %s %d %s", ip.getHostAddress(), port, username);
            int msgLength = msg.length() + 4;
            msg = String.format("%04d", msgLength) + msg;
            byte[] data = msg.getBytes();

            DatagramPacket request = new DatagramPacket(data, data.length, bsIP, 55555);
            sock.send(request);

             while(true){
                 byte[] buffer = new byte[65536]; // 64 KB message size
                 DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                 sock.receive(incoming);

                 byte[] data_recieve = incoming.getData();
                 s = new String(data_recieve, 0, incoming.getLength());

                 //echo the details of incoming data - client ip : client port - client message
                 echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

                 s.replace("\n", "");
                 StringTokenizer st = new StringTokenizer(s, " ");

                 String length = st.nextToken();
                 String command = st.nextToken();

                 String reply;

                 if(Integer.parseInt(length) != s.length()){
                     echo("Expected length : " + s.length() + ", Found: " + length);
                     reply = "0015 REGOK 9999";
                     DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                     sock.send(dpReply);
                 }else {
                    int statusCode;
                    switch(command){
                        case "REGOK":
                            statusCode = Integer.parseInt(st.nextToken());
                            if(statusCode != 9999 && statusCode != 9998 && statusCode != 9997 && statusCode != 9996){
                                // will 0 or 1 or 2 or 3 nodes to its peer table
                                for(int n = 0; n < statusCode; n++){
                                    String peerIP = st.nextToken();
                                    int peerPort = Integer.parseInt(st.nextToken());
                                    peerTable.add(new Node(peerIP, peerPort));
                                    String joinMsg = "JOIN "+ ip.getHostAddress() + " " + port;
                                    joinMsg = String.format("%04d", joinMsg.length() + 5) + " " + joinMsg;
                                    sock.send(new DatagramPacket(joinMsg.getBytes(), joinMsg.getBytes().length, InetAddress.getByName(peerIP), peerPort));
                                }
                            }
                            echo(String.format("Have %d peer nodes", peerTable.size()));
                            break;
                        case "ECHO":
                            for (int i=0; i<peerTable.size(); i++) {
                                echo(peerTable.get(i).getIp() + " " + peerTable.get(i).getPort());
                            }
                            reply = "0012 ECHOK 0";
                            sock.send(new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress(), incoming.getPort()));
                            break;
                        case "JOIN":
                            String peerIP = st.nextToken();
                            int peerPort = Integer.parseInt(st.nextToken());
                            peerTable.add(new Node(peerIP, peerPort));
                            reply = "0013 JOINOK 0";
                            sock.send(new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort()));
                            break;
                        case "JOINOK":
//                            echo(command + " " + st.nextToken());
                            break;
                        case "TRIGGER_LEAVE":
                            echo("READY TO LEAVE");
                            // unregister the peer itself from the bootstrap server
                            String unregMsg = "UNREG "+ ip.getHostAddress() + " " + port;
                            unregMsg = String.format("%04d", unregMsg.length() + 5) + " " + unregMsg;
                            sock.send(new DatagramPacket(unregMsg.getBytes(), unregMsg.getBytes().length, bsIP, 55555));

                            // when unreg ok receives we are ready to send leave msgs to neighbour peers to
                            break;
                        case "UNROK":
                            echo("UNROK " + st.nextToken());
                            // TODO:  need to check st.nextToken is 0 or 9999
                            String leaveMsg = "LEAVE "+ ip.getHostAddress() + " " + port;
                            leaveMsg = leaveMsg.format("%04d", leaveMsg.length() + 5) + " " + leaveMsg;
                            for(int j = 0; j < peerTable.size(); j++){
                                // sending the leave msg
                                sock.send(new DatagramPacket(leaveMsg.getBytes(), leaveMsg.getBytes().length, InetAddress.getByName(peerTable.get(j).getIp()), peerTable.get(j).getPort()));
                            }

                            // safely exit
                            System.exit(0);
                            break;
                        case "LEAVE":
                            String toRemoveIP = st.nextToken();
                            int toRemovePort = Integer.parseInt(st.nextToken());
                            echo("GOT LEAVE MSG FROM " + toRemoveIP + " " + toRemovePort);

                            for(int j = 0; j < peerTable.size(); j++){
                                if(peerTable.get(j).getIp().equals(toRemoveIP) && peerTable.get(j).getPort() == toRemovePort){
                                    peerTable.remove(j);
                                    echo("PEER NODE REMOVED: " + toRemoveIP + " " + toRemovePort);
                                    String leaveOK = "0014 LEAVEOK 0";
                                    sock.send(new DatagramPacket(leaveOK.getBytes(), leaveOK.getBytes().length, incoming.getAddress(), incoming.getPort()));
                                }
                            }
                            break;
                        case "LEAVEOK":
//                            echo("LEAVEOK " + st.nextToken());
                            break;
                        case "SER":
                            ser_msg_count++;
                            String ipInCmd = st.nextToken();
                            String portInCmd = st.nextToken();
                            String next = st.nextToken();
                            String searchQuery = next;
                            while(next.charAt(next.length()-1) != '"'){
                                next = st.nextToken();
                                searchQuery += " " + next;
                            }
                            int hops = Integer.parseInt(st.nextToken());
                            searchQuery = searchQuery.substring(1, searchQuery.length()-1).toLowerCase();
                            echo("Need to search : " + searchQuery + ", hops : " + hops);

                            if(hops > 0){
                                ArrayList<String> arr = search(searchQuery);
                                echo(arr.size() + " MATCHES FOUND");
                                hops--;
                                if(arr.size() == 0){
                                    for(int j = 0; j < peerTable.size(); j++){
                                        // search except the incoming node
                                        if(peerTable.get(j).getIp() != incoming.getAddress().getHostName() && peerTable.get(j).getPort() != incoming.getPort() && hops > 0){
                                            fwd_msg_count++;
                                            String searchMsg = "SER " + peerTable.get(j).getIp() + " " + peerTable.get(j).getPort() + " \"" + searchQuery + "\" " + hops;
                                            searchMsg = String.format("%04d", searchMsg.length() + 5) + " " + searchMsg;
                                            echo(searchMsg);
                                            Thread.sleep(1000);
                                            sock.send(new DatagramPacket(searchMsg.getBytes(), searchMsg.getBytes().length, InetAddress.getByName(peerTable.get(j).getIp()), peerTable.get(j).getPort()));
                                        }
                                    }
                                }else if(arr.size() > 0){
                                    ans_msg_count++;
                                    String serReply = "SEROK " + arr.size() + " " + ip.getHostAddress() + " " + port;
                                    for(String file : arr){
                                        serReply += " " + "\"" + file + "\"";
                                    }
                                    serReply += " " + (3-hops);
                                    serReply = String.format("%04d", serReply.length() + 5) + " " + serReply;
                                    sock.send(new DatagramPacket(serReply.getBytes(), serReply.getBytes().length, bsIP, bsPort));
                                }
                            }else{
                                echo("HOPS COUNT REACHED 0");
                            }
                            break;
                        case "DOWNLOAD":
                            ipInCmd = st.nextToken();
                            portInCmd = st.nextToken();

                            String n = st.nextToken();
                            String name = n;
                            while(n.charAt(n.length()-1) != '"'){
                                n = st.nextToken();
                                name += " " + n;
                            }

                            if(assignedFiles.contains(name.substring(1, name.length()-1))){
                                Random r = new Random();
                                int x = 1 + r.nextInt(10);
                                byte[] bytes = new byte[x*1024*1024]; // x MB size byte array
                                r.nextBytes(bytes);
                                String digest;
                                try {
                                    MessageDigest md = MessageDigest.getInstance("MD5");
                                    byte[] hash = md.digest(bytes);

                                    //converting byte array to Hexadecimal String
                                    StringBuilder sb = new StringBuilder(2*hash.length);
                                    for(byte b : hash){
                                        sb.append(String.format("%02x", b&0xff));
                                    }
                                    digest = sb.toString();

                                    echo(name + " : " + x + "MB : " + digest);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                FileOutputStream fr = new FileOutputStream("./send_" + name.substring(1, name.length()-1) +  "_" + new Date().getTime()+".mp3");
                                fr.write(bytes, 0, bytes.length);

                                Thread t = new Thread() {
                                    public void run() {
                                        echo("BEFORE FILE SEND");
                                        try {

                                            Socket sr = server.accept();
                                            OutputStream os = sr.getOutputStream();
                                            os.write(bytes, 0, bytes.length);
                                            echo("FILE SENT");

                                            // close the socket
                                            if(!sr.isClosed()){
                                                sr.close();
                                            }
                                        } catch (IOException e) {
//                                        e.printStackTrace();
                                        }
                                    }
                                };

                                t.start();
                                Thread.sleep(1000);

                                echo("DOWNLOAD READY ACK SENT");
                                // send udp ack to accept the tcp sent file
                                String download_ok_msg = "DWNLDOK " + ip.getHostAddress() + " " + port + " " + name;
                                download_ok_msg = String.format("%04d", download_ok_msg.length() + 5) + " " + download_ok_msg;
                                sock.send(new DatagramPacket(download_ok_msg.getBytes(), download_ok_msg.getBytes().length, bsIP, bsPort));
                            }else{
                                echo("FILE NOT FOUND");
                            }
                            break;
                        case "SAVE_LOG":
                            try {
                                File tempfile = new File("./node_" + Peer.port + ".txt");
                                tempfile.createNewFile();
                                String text = "SER_MSG_CNT : " + ser_msg_count + "\n" +
                                              "FWD_MSG_CNT : " + fwd_msg_count + "\n" +
                                              "ANS_MSG_CNT : " + ans_msg_count + "\n" +
                                              "NODE_DEGREE : " + peerTable.size() + "\n" ;

                                Files.write(Paths.get("./node_" + Peer.port + ".txt"), text.getBytes(), StandardOpenOption.APPEND);
                            }catch (IOException e) {
                                //exception handling left as an exercise for the reader
                            }
                            break;
                    }
                 }
             }


        }catch(IOException e){
            System.err.println("IOException " + e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }

    public static ArrayList<String> search(String query){
        ArrayList<String> arr = new ArrayList();
        for(String s : assignedFiles){
            String[] tokens = s.toLowerCase().split("\\s");

            String[] q_tokens = query.toLowerCase().split("\\s");
            boolean b = false;
            for(String t : tokens){
                for(String tk: q_tokens){
                    if(t.equals(tk)){
                        b = true;
                    }
                }
            }
            if(b){
                arr.add(s);
            }
        }
        return arr;
    }

    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    public static String[] shuffleArray(String[] arr) {
        for(int i = 0; i < arr.length; i++){
            Random rand = new Random();
            int j = i + rand.nextInt(arr.length-i);
            String temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

}

class Node{
    private String ip;
    private int port;

    Node(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }
}










