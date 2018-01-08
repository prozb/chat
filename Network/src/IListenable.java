/**
 * Interface must be implemented from your listener
 * and it doesn't matted whether is it client or server.
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
    void receveMessage(Connection connection, String value);

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
}
