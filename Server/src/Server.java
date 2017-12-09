import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements IListenable{
    private int port;
    public static void main(String[] args) {
            new Server();
    }
    //all connections are in this collection
    private ArrayList<Connection> connections = new ArrayList<>();

    private Server(){
        //by default is port 6666
        port = 6666;
        System.out.println("server started...");
        while (true){
            try (ServerSocket serverSocket = new ServerSocket(port);){
                Socket socket = serverSocket.accept();
                connected(new Connection(socket, this));

            } catch (IOException e) {
                System.out.println("connection cannot be established.");
            }
        }
    }
    @Override
    public void isExcepted(Connection connection, Exception e) {
        System.out.println(connection.toString() + " excepted: " + e);
    }

    @Override
    public void receveMessage(Connection connection, String value) {
        System.out.println(connection.toString() + " : " + value);
        sendOnAll(connection, connection.toString() + " : " + value);
    }

    @Override
    public void connected(Connection connection) {
        connections.add(connection);
        System.out.println(connection.toString() + " connected.");
        sendOnAll(connection, connection.toString() + " connected.");
        connection.sendString("welcome in this chat! ");
    }

    @Override
    public void disconnectClient(Connection connection) {
        connections.remove(connection);
        System.out.println(connection.toString() + " disconnected.");
        sendOnAll(connection, connection.toString() + " disconnected.");
    }

    /**
     * Method sends message on all clients
     * @param connection From which client is this message.
     * @param msg Message
     */
    public void sendOnAll(Connection connection, String msg){
        //send message on all clients except one client
        for(Connection val : connections){
            if(!val.equals(connection)){
                val.sendString(msg);
            }
        }
    }
}
