import Network.Protocol;
import Network.Protocol.Command;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import javax.swing.JOptionPane;

public class Client extends Thread {

	private static final String HOST = "localhost";
	private static final int PORT = 3131;
	/**
	 * Dieser Socket wird die Verbindung zwischen Server und Client darstellen
	 */
	public Socket socket = null;
	public String name;
	boolean interrupted = false;
	static PrintWriter toServer;
	static BufferedReader fromServer;

	String tosend = "";
	String commando = "";
	String user = "";
	String rest = "";
	String[] split;

/**
 * @param name - the name of the client
 * @throws ConnectException - in case there is no connection to server
 */
 public ChatClient(String name) throws ConnectException {
	 this.name = nick; 
	 
	try {
		socket = new Socket(HOST,PORT);
		toServer = new PrintWriter(socket.getOutputStream());
		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
	} catch (IOException e) {
		JOptionPane.showMessageDialog(null, "Verbindung zum Server fehlgeschlagen");
		e.printStackTrace();	
		
	}
	
}

	/**as long as the flag is not set up, run method is asking for lines
	 * as commands. The flag will be set up, if server sends the command refused.
	 * 
	 * @exception IO-Exception in case of readline().
	 */
	public void run() {

		while (!interrupted) {

			if (!socket.isClosed()) {

				try {
					tosend = fromServer.readLine();

					split = tosend.split(":");

					commando = split[0];

					Command cmd = Protocol.conclude(commando);

					switch (cmd) {

					case connect:
						updateUser();
						break;

					case message:
						user = split[1];
						rest = tosend.substring(commando.length() + user.length() + 2);
						break;

					case namelist:
						updateUser();
						break;

					case refused:
						rest = split[1];
						interrupted = true;
						break;
					}
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * the method sends the connect-command using format: "connect:NAME"
	 * @throws ConnectException in case server is not online
	 */
	public void connect() throws ConnectException {
		try {
			toServer.write("connect:" + this.name + '\n');
			toServer.flush();
		} catch (ConnectException e) {}	
	}

	/**
	 * the method sends the disconnect command using format "disconnect:NAME"
	 * socket and all streams are closed afterwards
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		try {
			toServer.write("disconnect:" + this.name + '\n');
			toServer.flush();
			toServer.close();
			socket.close();
			fromServer.close();
		} catch (IOException e) {}	
	}

	/**
	 * the method sends a message using format "message:NAME:NACHRICHT"
	 * @param rest contains the message of the client
	 */
	public void message(String rest) {
		toServer.write("message:" + this.name + ":" + rest + '\n');
		toServer.flush();
	}

	/**
	 * the method takes the usernames out of the list the server sends and adds them 
	 * to an array 
	 */
	public void updateUser() {
		String[] names = new String[split.length - 1];
		for (int i = 1; i < split.length; i++) {
			names[i - 1] = split[i];
			System.out.println(names[i - 1]);
		}
	}

}
