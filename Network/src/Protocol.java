
public class Protocol {

	public static enum Command {
		message, connect, disconnect, namelist, refused
	}
	
	/**
	 * method for the concluding of the protocol
	 */
	public static Command conclude(String s) {

		Command cmd;
		switch (s) {

		case "message":
			cmd = Command.message;
			break;

		case "namelist":
			cmd = Command.namelist;
			break;

		case "disconnect":
			cmd = Command.disconnect;
			break;

		case "connect":
			cmd = Command.connect;
			break;

		default:
			cmd = Command.refused;

		}
		return cmd;
	}
}