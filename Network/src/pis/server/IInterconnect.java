package pis.server;

import java.net.ServerSocket;

/**
 * Object implemented this interface must show message on gui
 * @version 1.0.0
 */
public interface IInterconnect {
    /**
     * Method shows string on gui
     * @param text Message to show on gui
     */
    void sendTextToGui(String text);
    /**
     * Method getter
     * @return returns serverSocket
     */
    ServerSocket getSocket();
}
