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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Peer{

    public static void main(String args[]){
        DatagramSocket sock = null;
        String s;
        List<Neighbour> nodes = new ArrayList<Neighbour>();

        try{
            // port of the peer
            int port = Integer.parseInt(args[0]);

            // username of the peer
            String username = args[1];

            sock = new DatagramSocket(port);
            echo("Peer is listening on port : " + sock.getLocalPort());

            InetAddress ip = InetAddress.getLocalHost();

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

                 StringTokenizer st = new StringTokenizer(s, " ");

                 String length = st.nextToken();
                 String command = st.nextToken();

                 if(command == "REGOK"){

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










