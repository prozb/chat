package pis.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Pavlo Rozbytskyi
 * @version 1.0.0
 */
public class Server implements IListenable {
    //all connections are in this collection
    private ArrayList<Connection> connections;
    //all names are in this collection
    private ArrayList<String> names;
    //file to save logs in
    private File log;
    private PrintWriter logMessage;
    private SimpleDateFormat dataFormat;
    private Date date;
    private IInterconnect gui;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public Server(IInterconnect gui){
        this.gui = gui;
        this.connections = new ArrayList<>();
        this.names = new ArrayList<>();
        this.dataFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        this.date = new Date();
        this.log = new File("log.txt");
        this.serverSocket = gui.getSocket();
        try {
            logMessage = new PrintWriter(new FileWriter(log, true));
        } catch (IOException e) {
            System.err.println("cannot create log.txt file.");
        }
        log("==============================================");
        log(String.format("current date: " + dataFormat.format(date)));
        log("server started.");

        this.serverThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    serverSocket.setSoTimeout(1000);
                } catch (IOException e) {
                    //Do nothing
                }
                try{
                    Socket socket = serverSocket.accept();
                    //number of connections must be restricted
                    if (connections.size() < Constants.MAX_CLIENTS_SIZE) {
                        Connection conn = new Connection(socket, Server.this);
                        conn.sendString("name list: " + createNamesList());
                        connected(conn);
                    } else {
                        Connection connection = new Connection(socket, Server.this);
                        connection.sendString("refused: too_many_users");
                        log("refused: too_many_users");
                        connection.disconnect();
                    }
                } catch (IOException e) {
                }

            }
            System.out.println("server stop");
            sendOnAll(null, "disconnect:");
            disconnectAllClients();
            log(String.format("server stopped at: " + dataFormat.format(date)));
            log("==============================================");
        });
        serverThread.start();
    }
    @Override
    public synchronized void isExcepted(Connection connection, Exception e) {
        log(connection.toString() + " excepted: " + e);
    }

    @Override
    public synchronized void receiveMessage(Connection connection, String value) {
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
        String name;
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
            //connection.interruptConnection();
            //connection.disconnect();
            names.remove(connection.getClientName());
            connections.remove(connection);
            log(getClientName(connection) + " disconnected.");
            sendOnAll(connection, getClientName(connection) + " disconnected.");
            sendOnAll("name list: " + createNamesList());
        }
    }

    @Override
    public synchronized void receiveNames(Connection connection) {
        connection.setNames(nameList());
        //send names on all users
        if(connection.isLoggedIn() && names.toString() != null){
            //making name list: [NAME]: {NAME} with simple manipulations
            String val = createNamesList();
            //send names on all clients
            for(Connection var : connections){
                if(var.getClientName() != null) {
                    var.sendString("name list: " + val);
                }
            }
            //log("name list: " + val);
        }
    }

    private String createNamesList(){
        String val = names.toString();
        val = val.replaceAll(",",":");
        val = val.replaceAll("\\[","");
        val = val.replaceAll("]","");
        return val;
    }

    @Override
    public synchronized void log(String msg) {
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
    private void sendOnAll(String msg){
        for(Connection val : connections){
            val.sendString(msg);
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

    /**
     * Method disconnects all clients and removes names of clients from the names collection
     */
    public void disconnectAllClients(){
        System.out.println("disconnecting all clients");

        for(int i = connections.size() - 1; i >= 0; i--){
            log(getClientName(connections.get(i)) + " disconnected.");
            names.remove(connections.get(i).getClientName());
            connections.get(i).disconnectServerSide();
            connections.remove(i);
        }
    }
    /**
     * Method finishes server thread
     */
    public void finish(){
        this.serverThread.interrupt();
        sendOnAll(null,"disconnect:");
    }
}