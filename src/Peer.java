/*
 *
 * compile and run : javac Peer.java; java Peer <port> <username>
 *
 *
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Peer{

    public static void main(String args[]){
        DatagramSocket sock = null;
        String s;
        List<Node> peerTable = new ArrayList<Node>();

        String[] possilbeFiles = {"Adventures of Tintin", "Jack and Jill", "Glee", "The Vampire Diarie", "King Arthur",
                "Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight", "Windows 8", "Mission Impossible",
                "Turn Up The Music", "Super Mario", "American Pickers", "Microsoft Office 2010", "Happy Feet", "Modern Family",
                "American Idol", "Hacking for Dummies" };

        List<String> availableFiles = new ArrayList<>();

        try{
            // port of the peer
            int port = Integer.parseInt(args[0]);

            // username of the peer
            String username = args[1];

            sock = new DatagramSocket(port);
            echo("Peer is listening on port : " + sock.getLocalPort());

            InetAddress ip = InetAddress.getLocalHost();

            // try to register the node at the BS
            String msg = String.format(" REG %s %d %s", ip.getHostAddress(), port, username);
            int msgLength = msg.length() + 4;
            msg = String.format("%04d", msgLength) + msg;
            byte[] data = msg.getBytes();

            DatagramPacket request = new DatagramPacket(data, data.length, ip, 55555);
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
                     echo(s.length()+"");
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
                                    peerTable.add(new Node(st.nextToken(), Integer.parseInt(st.nextToken())));
                                }
                            }
                            echo(String.format("Have %d peer nodes", peerTable.size()));
                            break;
                        case "ECHO":
                            for (int i=0; i<peerTable.size(); i++) {
                                echo(peerTable.get(i).getIp() + " " + peerTable.get(i).getPort());
                            }
                            reply = "0012 ECHOK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                            sock.send(dpReply);
                            break;
                    }
                 }
             }


        }catch(IOException e){
            System.err.println("IOException " + e);
        }

    }

    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
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










