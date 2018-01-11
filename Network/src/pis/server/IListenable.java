package pis.server;

/**
 * Interface must be implemented from your listener
 * and it doesn't matted whether is it client or server.
 *
 * @version 1.0.0
 */
public interface IListenable {
    /**
     *
     * @param connection Connection in which is exception
     * @param e Exception
     */
    void isExcepted(Connection connection, Exception e);

    /**
     *
     * @param connection Connection or client, who sent message
     * @param value Message sent from client
     */
    void receiveMessage(Connection connection, String value);

    /**
     * Method adds connection.
     * @param connection Add this connection.
     */
    void connected(Connection connection);

    /**
     * Method disconnects this client.
     * @param connection Connection must be broken.
     */
    void disconnectClient(Connection connection);

    /**
     * sends on the connection used names
     * @param connection for which connection must be used
     */
    void receiveNames(Connection connection);

    /**
     * Method is using to log all messages
     * @param msg message to log
     */
    void log(String msg);
}
