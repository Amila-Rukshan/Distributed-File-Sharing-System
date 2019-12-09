/*
 *
 * compile and run : javac BootstrapServer.java; java BootstrapServer
 *
 */

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class BootstrapServer {

    public static final int MAX_PEERS = 30;

    public static void main(String args[])
    {
        DatagramSocket sock = null;
        ServerSocket ss = null;
        String s;
        List<Neighbour> nodes = new ArrayList<Neighbour>();

        try
        {   // udp socket
            sock = new DatagramSocket(55555);

            // tcp socket
//            ss = new ServerSocket(55555);
//            Socket sr = ss.accept();

            echo("Bootstrap Server is listening on port : " + sock.getLocalPort());

            while(true)
            {
                byte[] buffer = new byte[65536]; // 64 KB message size
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());

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
                }else{
                    switch(command){
                        case "REG":
                            reply = "REGOK ";
                            if(nodes.size() < BootstrapServer.MAX_PEERS) {
                                String ip = st.nextToken();
                                int port = Integer.parseInt(st.nextToken());
                                String username = st.nextToken();
                                if (nodes.size() == 0) {
                                    reply += "0";
                                    nodes.add(new Neighbour(ip, port, username));
                                } else {
                                    boolean isOkay = true;
                                    for (int i = 0; i < nodes.size(); i++) {
                                        if (nodes.get(i).getPort() == port) {
                                            if (nodes.get(i).getUsername().equals(username)) {
                                                // already registered to you
                                                reply += "9998";
                                            } else {
                                                // registered to another user
                                                reply += "9997";
                                            }
                                            isOkay = false;
                                        }
                                    }

                                    if (isOkay) {
                                        if (nodes.size() == 1) {
                                            reply += "1 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort();
                                            echo("" + nodes.get(0).getPort());
                                        } else if (nodes.size() == 2) {
                                            reply += "2 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " + nodes.get(1).getPort();
                                            echo(nodes.get(0).getPort() + " " + nodes.get(1).getPort());
                                       } else if (nodes.size() == 3) {
                                            reply += "3 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " + nodes.get(1).getPort() + " " + nodes.get(2).getIp() + " " + nodes.get(2).getPort();
                                            echo(nodes.get(0).getPort() + " " + nodes.get(1).getPort() + " " + nodes.get(2).getPort());
                                       } else {
                                            Random r = new Random();
                                            int Low = 0;
                                            int High = nodes.size();
                                            int random_1 = r.nextInt(High - Low) + Low;
                                            int random_2 = r.nextInt(High - Low) + Low;
                                            while (random_1 == random_2) {
                                                random_2 = r.nextInt(High - Low) + Low;
                                            }
                                            echo(nodes.get(random_1).getPort() + " " + nodes.get(random_2).getPort());
                                            reply += "2 " + nodes.get(random_1).getIp() + " " + nodes.get(random_1).getPort() + " " + nodes.get(random_2).getIp() + " " + nodes.get(random_2).getPort();
                                        }
                                        nodes.add(new Neighbour(ip, port, username));
                                    }
                                }

                                reply = String.format("%04d", reply.length() + 5) + " " + reply;

                                DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                                sock.send(dpReply);
                            }else{
                                reply = "0015 REGOK 9996";

                                DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                                sock.send(dpReply);
                            }
                            break;
                        case "UNREG":
                            String ip = st.nextToken();
                            int port = Integer.parseInt(st.nextToken());
                            for (int i=0; i<nodes.size(); i++) {
                                if (nodes.get(i).getIp().equals(ip) && nodes.get(i).getPort() == port) {
                                    nodes.remove(i);
                                    echo("SUCCESSFULLY UNREGISTERED NODE : " + ip + " " + port);
                                    reply = "0012 UNROK 0";
                                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                                    sock.send(dpReply);
                                }
                            }
                            break;
                        case "ECHO":
                            for (int i=0; i<nodes.size(); i++) {
                                echo(nodes.get(i).getIp() + " " + nodes.get(i).getPort() + " " + nodes.get(i).getUsername());
                            }
                            reply = "0012 ECHOK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                            sock.send(dpReply);
                            break;

                        case "SEROK":
                            int file_count = Integer.parseInt(st.nextToken());
                            String ipInCmd = st.nextToken();
                            String portInCmd = st.nextToken();
                            for(int k = 0; k < file_count; k++){
                                String next = st.nextToken();
                                String searchQuery = next;
                                while(next.charAt(next.length()-1) != '"'){
                                    next = st.nextToken();
                                    searchQuery += " " + next;
                                }
//                                secureSock
//                                downloadMsg = String.format("%04d", searchMsg.length() + 5) + " " + searchMsg;
                            }
                            break;
                    }
                }
            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);

        }
    }

    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
