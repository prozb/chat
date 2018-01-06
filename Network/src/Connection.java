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
class Connection{

    private Socket socket;
    private Thread connectionThread;
    private BufferedWriter out;
    private BufferedReader in;
    private IListenable actionListener;
    private String clientName;
    private boolean loggedIn;
    private boolean disconnectedFlag;
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
        //you need this stuff to avoid stack overflow when client
        //makes hard disconnect (closes terminal)
        this.disconnectedFlag = false;
        //in this list are there all clients names
        this.names = new ArrayList<>();
        //creation new thread
        //this.connectionThread.setDaemon(true);
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
                    } catch (Exception e) {
                        System.out.println("disconnected:");
                    }
                }
            }
        });
        connectionThread.setDaemon(true);
        //starting new thread
        connectionThread.start();
    }

    //method processes all messages from clients
    private void processInMessages(String msg) throws Exception{
        //help command processing
        if(helpCommand(msg)){
            sendString("tape \"connect: [NAME]\" to log in.\n\rtape \"disconnect: \" to disconnect\n\r" +
                            "tape \"message: [MESSAGE]\" to send message (must be logged in)\n\r");
        }else
        //disconnect command processing
        if(disconnectedCommand(msg)){
            disconnect();
        }else
        //message command processing
        if(messageCommand(msg) && isLoggedIn()){
            actionListener.receveMessage(this, msg.replaceAll("message: ", ""));
        }else
        //receive names from server
        //actionListener.receiveNames(this);
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
            actionListener.log("user " + socket.getInetAddress() + " is trying to use chosen name.");
        }else if(!isLoggedIn() && connectedCommand(msg) && !correctName(msg) && !nameExists(msg)){
            sendString("refused: invalid_name");
            actionListener.log("user " + socket.getInetAddress() + " is trying to use chosen name.");
        }else if(isLoggedIn() && connectedCommand(msg) && correctName(msg) && !nameExists(msg)){
            sendString("refused: cannot_change_name");
            actionListener.log("user " + socket.getInetAddress() + " is trying change his name.");
        }else{
            sendString("refused: see_how_to_use_chat");
            actionListener.log((!isLoggedIn() ? socket.getInetAddress() : clientName) + " wrong_command");
        }
    }
    //connection proves whether this name exists or not
    private boolean nameExists(String val){
        actionListener.receiveNames(this);
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
            if(!socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                //write msg in buffer
                out.write(msg + "\r\n");
                //make buffer empty and send string
                out.flush();
            }else {
                disconnect();
            }
        } catch (Exception e) {
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

        //flag to avoid stack overflow
        //if user just closes terminal disconnect method cannot
        //figure out, that socket is closed, even if you use socket.isClosed()
        disconnectedFlag = true;
        if(!socket.isClosed() && !disconnectedFlag){
            sendString("disconnect: ok");
        }
        //interrupt thread and stop connection
        connectionThread.interrupt();
        actionListener.disconnectClient(this);
        try {
            socket.close();
        } catch (Exception e) {
            //send problem on listener
            actionListener.isExcepted(this, e);
            actionListener.receveMessage(this, toString() + " disconnectedCommand.");
        }
    }
    //=====================GETTERS & SETTERS=========================
    public void interruptConnection(){
        connectionThread.interrupt();
    }
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

    /**
     * Socket getter
     * @return returns socket for this connection
     */
    public Socket getSocket() {
        return socket;
    }

    //=====================GETTERS & SETTERS=========================
}
