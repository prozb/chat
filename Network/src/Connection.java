import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class Connection describes connection of each client independent
 * of each other in separate thread.
 */
public class Connection{

    private Socket socket;
    private Thread connectionThread;
    private BufferedWriter out;
    private BufferedReader in;
    private IListenable actionListener;
    private String clientName;
    private boolean registrated;
    private String connectPattern;
    private Pattern pattern;
    private Matcher matcher;
    private ArrayList<String> names;
    //private boolean isPlace;

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
        //this.clientName = "pidor";
        //user must be registrated to send messages
        this.registrated = false;
        this.names = new ArrayList<>();
        //creation new thread
        //this.connectPattern = "^" + Constants.CONNECT + ;
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

    private void processInMessages(String msg){
        if(helpCommand(msg)){
            sendString("tape \"command: [NAME]\" to log in.\n\rtape \"disconnect: \" to disconnect\n\r" +
                            "tape \"message: [MESSAGE]\" to send message (must be logged in)\n\r");
        }
        if(disconnectedCommand(msg))
            disconnect();

        if(registrated){
            if(connectedCommand(msg)){
                System.out.println("user " + clientName + " is trying to change name.");
                sendString("you cannot change your name. ");
            }else if(messageCommand(msg)){
                actionListener.receveMessage(this, msg.replaceAll("message: ", ""));
            }
        }else {
            //if user isn't registrated and used correct command,
            //register this user
            if(!registrated && connectedCommand(msg)){
                actionListener.receiveNames(this);
                //System.out.println(names.toString());
                if(!nameExists(msg.replaceAll("connect:\\s", ""))) {
                    registrated = true;
                    sendString("connect: ok");
                    this.clientName = msg.replaceAll("connect:\\s", "");
                    //sendString("Welcome in this chat dear " + clientName);
                    actionListener.receveMessage(this, "logged in successfully!");
                }else{
                    System.out.println("user " + socket.getInetAddress() + " is trying to use choosen name.");
                    sendString("refused: name_in_use");
                }
            }else{
                System.out.println("user " + socket.getInetAddress() + " is trying to register.");
                sendString("please, confirm registration in form \"connect: USER_NAME\"");
            }
        }
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
        pattern = Pattern.compile("^connect: [a-zA-Z0-9_]{3,30}");
        matcher = pattern.matcher(val);
        return matcher.matches();
    }
    /**
     * Method sends String on client
     * @param msg String must be sent
     */
    public synchronized void sendString(String msg){
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
    public synchronized void disconnect(){
        //if disconnect, interrupt thread and close socket
        if(!socket.isClosed()){
            sendString("disconnect: ok");
            //System.out.println("disconnect: ok");
        }

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

    /**
     * @return ip address of the client an his name
     */
    @Override
    public String toString() {
        return socket.getInetAddress() + "";
    }

    /**
     *
     * @return Whether or not is the client registrated
     */
    public boolean isRegistrated() {
        return registrated;
    }

    /**
     *
     * @return Clients name
     */
    public String getClientName(){
        return clientName;
    }

    /**
     *
     * @param names collection with names of all users in chat
     */
    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    /**
     * @param val name to check in collection
     * @return whether or not name exists
     */
    private boolean nameExists(String val){
        for(String name : names){
            if(name != null && val.equals(name)){
                return true;
            }
        }
        return false;
    }
}
