package client.command;

public class CommandExecute extends Exception {
	private static final long serialVersionUID = 1L;

	public CommandExecute() {
		super();
	}

	public  CommandExecute(String message) {
		super(message);
	}

	public CommandExecute(int expectedArguments) {
		super("指令至少要 " + expectedArguments + " 個參數");
	}
}
