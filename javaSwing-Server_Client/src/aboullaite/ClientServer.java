package aboullaite;

import javax.swing.*;

//Class to precise who is connected : Client or Server
//실행 프로그램, 사용자가 server client 둘 중 하나를 선택하게 하며 해당 mode를 실행하게 한다.  
public class ClientServer {

	public static void main(String[] args) {

		Object[] selectioValues = { "Server", "Client" };
		String initialSection = "Server";

		Object selection = JOptionPane.showInputDialog(null, "Login as : ",
				"MyChatApp", JOptionPane.QUESTION_MESSAGE, null,
				selectioValues, initialSection);
		if (selection.equals("Server")) {
			String[] arguments = new String[] {};
			// 사용자가 server를 실행한 경우
			new MultiThreadChatServerSync().main(arguments);
		} else if (selection.equals("Client")) {
			String IPServer = JOptionPane
					.showInputDialog("Enter the Server ip adress");
			String[] arguments = new String[] { IPServer };
			// 사용자가 client를 실행한 경우
			// clientThread는 ChatClient와 관계가 없다.
			new ChatClient().main(arguments);
		}

	}

}
