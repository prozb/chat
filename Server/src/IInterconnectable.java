import java.net.ServerSocket;

public interface IInterconnectable {
    void sendTextToGui(String text);
    ServerSocket getSocket();
}
