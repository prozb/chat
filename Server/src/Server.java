import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
//TODO: This Thread is daemon. Make sure killing this daemon is working properly
/**
 * @author Pavlo Rozbytskyi
 * @version 1.0.0
 */
public class Server implements IListenable{
    private int port;
    //all connections are in this collection
    private ArrayList<Connection> connections;
    //all names are in this collection
    private ArrayList<String> names;
    private File log;
    private PrintWriter logMessage;
    private SimpleDateFormat dataFormat;
    private Date date;
    private IInterconnectable gui;
    private ServerSocket serverSocket;
    private boolean isRunning;
    //private volatile boolean isRunning;
    private Thread serverThread;


    public Server(IInterconnectable gui){
        this.isRunning = true;
        this.gui = gui;
        connections = new ArrayList<>();
        names = new ArrayList<>();
        dataFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        date = new Date();
        //all stuff for logging
        log = new File("log.txt");
        try {
            logMessage = new PrintWriter(new FileWriter(log, true));
        } catch (IOException e) {
            System.err.println("cannot create log.txt file.");
        }
        log("=====================================");
        log(String.format("current date: " + dataFormat.format(date)));
        //by default is port 6666
        port = Constants.DEFAULT_PORT;
        log("server started.");

        this.serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("running");
                    try {
                        serverSocket = new ServerSocket(port);
                        serverSocket.setSoTimeout(1000);
                    } catch (IOException e) {
                    }

                    try{
                        Socket socket = serverSocket.accept();
                        //number of connections must be restricted
                        if (connections.size() < Constants.MAX_CLIENTS_SIZE) {
                            connected(new Connection(socket, Server.this));
                        } else {
                            Connection connection = new Connection(socket, Server.this);
                            connection.sendString("refused: too_many_users");
                            log("refused: too_many_users");
                            connection.disconnect();
                        }
                    } catch (IOException e) {
                        //log("connection cannot be established.");
                    }

                }
                System.out.println("server stop");
                //disconnectAllClients();
                sendOnAll(null, "disconnect:");
                //disconnectClient(connections.get(0));
                log(String.format("server stopped at: " + dataFormat.format(date)));
            }
        });
        serverThread.start();
    }
    @Override
    public synchronized void isExcepted(Connection connection, Exception e) {
        log(connection.toString() + " excepted: " + e);
    }

    @Override
    public synchronized void receveMessage(Connection connection, String value) {
        log(getClientName(connection) + ": " + value);
        sendOnAll(connection, getClientName(connection) + " : " + value);
    }

    @Override
    public synchronized void connected(Connection connection) {
        connections.add(connection);
        log(getClientName(connection) + " connected.");
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
            connection.interruptConnection();
            connections.remove(connection);
            log(getClientName(connection) + " disconnected.");
            sendOnAll(connection, getClientName(connection) + " disconnected.");
        }
    }

    @Override
    public void receiveNames(Connection connection) {
        connection.setNames(nameList());
        //send names on all users
        if(connection.isLoggedIn() && names.toString() != null){
            //making name list: [NAME]: {NAME} with simple manipulations
            String val = names.toString();
            val = val.replaceAll(",",":");
            val = val.replaceAll("\\[","");
            val = val.replaceAll("]","");
            //send names on all clients
            for(Connection var : connections){
                if(var.getClientName() != null) {
                    var.sendString("name list: " + val);
                }
            }
            log("name list: " + val);
        }
    }

    @Override
    public void log(String msg) {
        System.out.println(msg);
        logMessage.write(msg + "\n");
        logMessage.flush();
        gui.sendTextToGui(msg);
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
    //=============================GETTERS & SETTERS======================================
    /**
     * @param connection
     * @return client name if client is logged in, otherwise return ip address
     */
    public String getClientName(Connection connection){
        return connection.getClientName() != null ? connection.getClientName() :
                connection.toString();
    }
    //=============================GETTERS & SETTERS======================================
    public void disconnectAllClients(){
        System.out.println("disconnecting all clients");
        for(Connection connection : connections){
            disconnectClient(connection);
        }
    }

    public void finish(){
        this.serverThread.interrupt();
        System.out.println("finished");
        sendOnAll(null,"disconnect:");
        this.serverSocket = null;
    }
}