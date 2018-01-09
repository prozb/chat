import java.io.*;
import java.net.Socket;

class Client{
    private ISendable gui;
    private String name;
    private final int PORT;
    private final String ip;
    private Socket socket;
    private Thread clientThread;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean loggedIn;

    public Client(ISendable gui) throws Exception{
        this.loggedIn = false;
        this.gui = gui;
        this.ip = "localhost";
        this.PORT = Constants.DEFAULT_PORT;
        this.socket = new Socket(ip, PORT);

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String msg;
                while (!clientThread.isInterrupted()){
                    try {
                        msg = in.readLine();
                        if(msg != null){
                            gui.sendTextToGui(msg);
                            System.out.println(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnect();
                    }
                }
            }
        });
        this.clientThread.setDaemon(true);
        this.clientThread.start();
    }

    private void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("socket closed");
        }
    }

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
        }
    }
}