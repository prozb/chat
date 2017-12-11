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
    private StringBuilder builder = new StringBuilder();

    private Server(){
        //by default is port 6666
        port = 6666;
        System.out.println("server started...");
        while (true){
            try (ServerSocket serverSocket = new ServerSocket(port);){
                Socket socket = serverSocket.accept();
                if(connections.size() < Constants.MAX_CLIENTS_SIZE) {
                    connected(new Connection(socket, this));
                }else{
                    Connection connection = new Connection(socket, this);
                    connection.sendString("refused: too_many_users");
                    System.out.println("refused: too_many_users");
                    connection.disconnect();
                }

            } catch (IOException e) {
                System.out.println("connection cannot be established.");
            }
        }
    }
    @Override
    public synchronized void isExcepted(Connection connection, Exception e) {
        System.out.println(connection.toString() + " excepted: " + e);
    }

    @Override
    public synchronized void receveMessage(Connection connection, String value) {
        System.out.println(getClientName(connection) + ": " + value);
        sendOnAll(connection, getClientName(connection) + " : " + value);
    }

    @Override
    public synchronized void connected(Connection connection) {
        connections.add(connection);
        System.out.println(getClientName(connection) + " connected.");
      /*  if(connection.isRegistrated()) {
            connection.sendString("connect: ok");
        }*/
        //connection.sendString("welcome in this chat! ");
        //connection.sendString(nameList());
        connection.sendString("please, confirm registration in form \"connect: USER_NAME\"");
    }

    private String nameList(){
        builder.setLength(0);
        builder.append("namelist");
        for(Connection val : connections) {
            if(val.isRegistrated() && val.getClientName() != null) {
                builder.append(": " + val.getClientName());
            }
        }
        return builder.toString();
    }

    @Override
    public synchronized void disconnectClient(Connection connection) {
        if(connections.contains(connection)) {
            connections.remove(connection);
            System.out.println(getClientName(connection) + " disconnected.");
            sendOnAll(connection, getClientName(connection) + " disconnected.");
        }
    }

    /**
     * Method sends message on all clients
     * @param connection From which client is this message.
     * @param msg Message
     */
    public void sendOnAll(Connection connection, String msg){
        //send message on all clients except one client
        for(Connection val : connections){
            if(!val.equals(connection) && val.isRegistrated()){
                val.sendString(msg);
            }
        }
    }

    public String getClientName(Connection connection){
        return connection.getClientName() != null ? connection.getClientName() :
                connection.toString();
    }

    /**
     * Proves if this client can have this name
     * @param connection Current connection
     * @param name Name to check, whether or not exists.
     * @return exist or no
     */
/*    public boolean checkName(Connection connection, String name){
        boolean exists = false;
        for(Connection var : connections){
            if(var.getClientName().equals(name))
                exists = true;
        }
        return exists;
    }*/
}
