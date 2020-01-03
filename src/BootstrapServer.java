/*
 *
 * compile and run : javac BootstrapServer.java; java BootstrapServer
 *
 */

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

                    String ipInCmd = "";
                    String portInCmd = "";

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
                            ipInCmd = st.nextToken();
                            portInCmd = st.nextToken();

                            String hops = "";
                            String searchQuery = "";
                            for(int k = 0; k < file_count; k++){
                                String next = st.nextToken();
                                searchQuery = next;
                                while(next.charAt(next.length()-1) != '"'){
                                    next = st.nextToken();
                                    searchQuery += " " + next;
                                }
//                                secureSock
//                                downloadMsg = String.format("%04d", searchMsg.length() + 5) + " " + searchMsg;
                                hops = st.nextToken();
                            }
                            try {
                                String text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " RES : " + ipInCmd + " " + portInCmd + " " + hops + " " + searchQuery + "\n";
                                Files.write(Paths.get("./query_log.txt"), text.getBytes(), StandardOpenOption.APPEND);
                            }catch (IOException e) {
                                //exception handling left as an exercise for the reader
                            }

                            break;
                        case "DOWNLOAD_TRIGGER":
                            ipInCmd = st.nextToken();
                            portInCmd = st.nextToken();

                            String n = st.nextToken();
                            String name = n;
                            while(n.charAt(n.length()-1) != '"'){
                                n = st.nextToken();
                                name += " " + n;
                            }

                            // send download msg
                            String downloadMsg = "DOWNLOAD " + ipInCmd + " " + portInCmd + " " + name;
                            downloadMsg = String.format("%04d", downloadMsg.length() + 5) + " " + downloadMsg;
                            sock.send(new DatagramPacket(downloadMsg.getBytes(), downloadMsg.getBytes().length, InetAddress.getByName(ipInCmd), Integer.parseInt(portInCmd)));

                            break;
                        case "DWNLDOK":
                            ipInCmd = st.nextToken();
                            portInCmd = st.nextToken();
                            Socket sr = new Socket(ipInCmd, Integer.parseInt(portInCmd));

                            String n2 = st.nextToken();
                            String name2 = n2;
                            while(n2.charAt(n2.length()-1) != '"'){
                                n2 = st.nextToken();
                                name2 += " " + n2;
                            }

                            InputStream is = sr.getInputStream();
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            int b;
                            while ((b = is.read()) != -1)
                                os.write(b);

                            byte[] bytes = os.toByteArray(); //new byte[10*1024*1024];
                            FileOutputStream fr = new FileOutputStream("./receive_" + name2.substring(1, name2.length()-1) + "_" + new Date().getTime() + ".mp3");

                            is.read(bytes, 0, bytes.length);
                            fr.write(bytes, 0, bytes.length);
                            echo("FILE RECEIVED");
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
