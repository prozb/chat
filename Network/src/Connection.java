import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class Connection describes connection of each client independent
 * of each other in separate thread.
 * @author Pavlo Rozbytskyi
 * @version 1.0.0
 */
public class Connection{

    private Socket socket;
    private Thread connectionThread;
    private BufferedWriter out;
    private BufferedReader in;
    private IListenable actionListener;
    private String clientName;
    private boolean loggedIn;
    private Pattern pattern;
    private Matcher matcher;
    private ArrayList<String> names;

    /**
     *
     * @param ip ip address for putty connection
     * @param port port for putty connection
     * @param actionListener
     * @throws IOException Exception if the connection can't be established.
     */
    public Connection(String ip, int port, IListenable actionListener) throws IOException {
        //constructor overloading
        this(new Socket(ip, port), actionListener);
    }
    /**
     *
     * @param socket Socket for each client connection.
     * @param actionListener Client or Server class can be listener.
     */
    public Connection(Socket socket, IListenable actionListener) throws IOException {
        this.socket = socket;
        this.actionListener = actionListener;
        //create streams to handle with data
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //user must be loggedIn to send messages
        this.loggedIn = false;
        //in this list are there all clients names
        this.names = new ArrayList<>();
        //creation new thread
        this.connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!connectionThread.isInterrupted()){
                    String msg = null;
                    try {
                        //read message from the stream
                        msg = in.readLine();
                        if(msg != null) {
                            //send message on listener
                            processInMessages(msg);
                        }else disconnect();
                    } catch (IOException e) {
                        //if cannot receive message, disconnect and handle exception
                        actionListener.isExcepted(Connection.this, e);
                        disconnect();
                    }
                }
            }
        });
        //starting new thread
        connectionThread.start();
    }

    //method processes all messages from clients
    private void processInMessages(String msg){
        //help command processing
        if(helpCommand(msg)){
            sendString("tape \"command: [NAME]\" to log in.\n\rtape \"disconnect: \" to disconnect\n\r" +
                            "tape \"message: [MESSAGE]\" to send message (must be logged in)\n\r");
        }
        //disconnect command processing
        if(disconnectedCommand(msg))
            disconnect();
        //message command processing
        if(messageCommand(msg) && isLoggedIn()){
            actionListener.receveMessage(this, msg.replaceAll("message: ", ""));
        }
        //receive names from server
        actionListener.receiveNames(this);
        //don't touch this stuff
        if(connectedCommand(msg) && !isLoggedIn() && correctName(msg) && !nameExists(msg)){
            loggedIn = true;
            sendString("connect: ok");
            this.clientName = msg.replaceAll("connect:\\s", "");
            //sendString("Welcome in this chat dear " + clientName);
            actionListener.receveMessage(this, "logged in successfully!");
            //be using method receiveNames, sends the server numbers on all connections
            actionListener.receiveNames(this);
        }else if(!isLoggedIn() && connectedCommand(msg) && correctName(msg) && nameExists(msg)){
            sendString("refused: name_in_use");
            System.out.println("user " + socket.getInetAddress() + " is trying to use chosen name.");
        }else if(!isLoggedIn() && connectedCommand(msg) && !correctName(msg) && !nameExists(msg)){
            sendString("refused: invalid_name");
            System.out.println("user " + socket.getInetAddress() + " is trying to use chosen name.");
        }else if(isLoggedIn() && connectedCommand(msg) && correctName(msg) && !nameExists(msg)){
            System.out.println("user " + socket.getInetAddress() + " is trying change his name.");
            sendString("refused: cannot_change_name");
        }else{
            System.out.println("user " + socket.getInetAddress() + " is trying to log in.");
            sendString("refused: see_how_to_use_chat");
        }
    }
    //connection proves whether this name exists or not
    private boolean nameExists(String val){
        //just take the name from message
        String valName = val.replaceAll("connect: ", "");
        for(String name : names){
            if(name != null && valName.equals(name)){
                return true;
            }
        }
        //return false if name doesn't exist
        return false;
    }
    //================checking with regex===========================
    private boolean correctName(String val){
        pattern = Pattern.compile("^[a-zA-Z0-9_]{3,30}");
        matcher = pattern.matcher(val.replaceAll("connect: ", ""));
        return matcher.matches();
    }
    private boolean helpCommand(String val){
        pattern = Pattern.compile("^help:");
        matcher = pattern.matcher(val);
        return matcher.matches();
    }
    private boolean messageCommand(String val){
        pattern = Pattern.compile("^message: .+");
        matcher = pattern.matcher(val);
        return matcher.matches();
    }
    private boolean disconnectedCommand(String val){
        pattern = Pattern.compile("^disconnect:$");
        matcher = pattern.matcher(val);
        return matcher.matches();
    }
    private boolean connectedCommand(String val){
        pattern = Pattern.compile("^connect: .{3,30}");
        matcher = pattern.matcher(val);
        return matcher.matches();
    }
    //================checking with regex===========================
    /**
     * Method sends String on client
     * @param msg String must be sent
     */
    public void sendString(String msg){
        try {
            //write msg in buffer
            out.write(msg+ "\r\n");
            //make buffer empty and send string
            out.flush();
        } catch (IOException e) {
            //in cannot be sent, disconnect and handle exception
            disconnect();
            actionListener.isExcepted(this, e);
        }
    }
    /**
     * Disconnecting method for each client.
     */
    public void disconnect(){
        //if socket isn't closed, send message disconnect
        //otherwise Stack overflow exception
        if(!socket.isClosed()){
            sendString("disconnect: ok");
        }
        //interrupt thread and stop connection
        connectionThread.interrupt();
        actionListener.disconnectClient(this);
        try {
            socket.close();
        } catch (IOException e) {
            //send problem on listener
            actionListener.isExcepted(this, e);
            actionListener.receveMessage(this, toString() + " disconnectedCommand.");
        }
    }

    //=====================GETTERS & SETTERS=========================
    /**
     * @return ip address of the client
     */
    @Override
    public String toString() {
        return socket.getInetAddress() + "";
    }
    /**
     * Logged in getter
     * @return Whether or not is the client loggedIn
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
    /**
     * Client name getter
     * @return Clients name
     */
    public String getClientName(){
        return clientName;
    }
    /**
     * Names setter
     * @param names collection with names of all users in chat
     */
    public void setNames(ArrayList<String> names) {
        this.names = names;
    }
    //=====================GETTERS & SETTERS=========================
}
