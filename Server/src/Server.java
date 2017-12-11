import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Pavlo Rozbytskyi
 * @version 1.0.0
 */
public class Server implements IListenable{
    private int port;
    public static void main(String[] args) {
        new Server();
    }
    //all connections are in this collection
    private ArrayList<Connection> connections = new ArrayList<>();
    //all names are in this collection
    private ArrayList<String> names = new ArrayList<>();

    private Server(){
        //by default is port 6666
        port = Constants.DEFAULT_PORT;
        System.out.println("server started.");
        while (true){
            try (ServerSocket serverSocket = new ServerSocket(port);){
                Socket socket = serverSocket.accept();
                //number of connections is be restricted
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
        connection.sendString("tape \"help:\" to use this chat. ");
    }
    //returns names of all logged in users
    private ArrayList<String> nameList(){
        names.clear();
        String name = null;
        for(Connection connection : connections){
            if(connection.isLoggedIn() && connection.getClientName() != null){
                name = connection.getClientName();
                names.add(name);
            }
        }
        return names;
    }

    @Override
    public synchronized void disconnectClient(Connection connection) {
        if(connections.contains(connection)) {
            connections.remove(connection);
            System.out.println(getClientName(connection) + " disconnected.");
            sendOnAll(connection, getClientName(connection) + " disconnected.");
        }
    }

    @Override
    public void receiveNames(Connection connection) {
        connection.setNames(nameList());
        //send names on all users
        if(connection.isLoggedIn() && names.toString() != null){
            //making namelist: [NAME]: {NAME} with simple manipulations
            String val = names.toString();
            val = val.replaceAll(",",":");
            val = val.replaceAll("\\[","");
            val = val.replaceAll("]","");
            //send names on all clients
            for(Connection var : connections){
                var.sendString("namelist: " + val);
            }
            System.out.println("namelist: " + val);
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
            if(!val.equals(connection) && val.isLoggedIn()){
                val.sendString(msg);
            }
        }
    }

    /**
     * @param connection
     * @return client name if client is logged in, otherwise return ip address
     */
    public String getClientName(Connection connection){
        return connection.getClientName() != null ? connection.getClientName() :
                connection.toString();
    }
}
