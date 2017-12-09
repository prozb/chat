import java.io.*;
import java.net.Socket;

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
        this.clientName = "pidor";
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
                            actionListener.receveMessage(Connection.this, msg);
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
        connectionThread.interrupt();
        actionListener.disconnectClient(this);
        try {
            socket.close();
        } catch (IOException e) {
            //send problem on listener
            actionListener.isExcepted(this, e);
            actionListener.receveMessage(this, toString() + " disconnected.");
        }
    }

    /**
     * @return ip address of the client an his name
     */
    @Override
    public String toString() {
        return socket.getInetAddress() + " " + clientName;
    }
}
