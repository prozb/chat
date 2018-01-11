package pis.client;

import pis.server.Constants;

import java.io.*;
import java.net.Socket;

/**
 * In this client class is logic of client - server interconnecting
 *
 * @version 1.0.0
 */
class Client{
    private ISend gui;
    private String name;
    private final int PORT;
    private final String ip;
    private Socket socket;
    private Thread clientThread;
    private BufferedReader in;
    private BufferedWriter out;

    /**
     * Constructor
     * @param gui to send messages from client to gui
     * @throws Exception
     */
    public Client(ISend gui) throws Exception{
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

        this.clientThread = new Thread(() -> {
            String msg;
            while (!clientThread.isInterrupted()){
                try {
                    msg = in.readLine();
                    if(msg != null){
                        gui.showTextOnGui(msg);
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                }
            }
        });
        this.clientThread.setDaemon(true);
        this.clientThread.start();
    }
    //just disconnect client from server
    private void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("socket closed");
        }
    }

    /**
     * Method sends messages from client to server
     * @param msg message to send
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
        }
    }
}