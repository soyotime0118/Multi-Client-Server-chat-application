package aboullaite;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// Class to manage Client chat Box.
public class ChatClient {

	/** Chat client access */
	// Observer pattern은 관찰대상과 관찰자 객체를 연결하는 패턴이다.
	// Observable은 관찰대상 객체가 상속받는다.
	// 이 프로젝트에서 관찰자는 ChatFrame 이다
	// 관찰대상 객체가 상태가 변경된 경우, 관찰자 객체에 이를 알린다.
	// 관찰자 객체는 update 메소드가 실행된다
	static class ChatAccess extends Observable {
		// client socket
		private Socket socket;
		private OutputStream outputStream;

		// 이 메소드가 호출될 경우, 관찰자에게 알림이 간다
		@Override
		public void notifyObservers(Object arg) {
			super.setChanged();
			super.notifyObservers(arg);
		}

		/** Create socket, and receiving thread */
		// 소켓을 생성하고 thread를 받는다
		public ChatAccess(String server, int port) throws IOException {
			// client 가 server와 연결할 socket 생성
			socket = new Socket(server, port);
			outputStream = socket.getOutputStream();

			// 다른 사용자가 채팅한 text를 수신하는 thread이다
			Thread receivingThread = new Thread() {
				@Override
				public void run() {
					try {
						// client socket에서 data가 들어올 경우 observer pattern의
						// notify가 발생한다.
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null)
							// 보여줘야 할 내용을 전달한다.
							notifyObservers(line);
					} catch (IOException ex) {
						notifyObservers(ex);
					}
				}
			};
			receivingThread.start();
		}

		private static final String CRLF = "\r\n"; // newline

		/** Send a line of text */
		/** 채팅 메시지를 전송하는 메소드 */
		public void send(String text) {
			try {
				outputStream.write((text + CRLF).getBytes());
				outputStream.flush();
			} catch (IOException ex) {
				notifyObservers(ex);
			}
		}

		/** Close the socket */
		public void close() {
			try {
				socket.close();
			} catch (IOException ex) {
				notifyObservers(ex);
			}
		}
	}

	/** Chat client UI */
	// UI 생성
	static class ChatFrame extends JFrame implements Observer {
		// UI 관련 부분
		private JTextArea textArea;
		private JTextField inputTextField;
		private JButton sendButton;

		//
		private ChatAccess chatAccess;

		public ChatFrame(ChatAccess chatAccess) {
			this.chatAccess = chatAccess;
			// observer pattern에서 관찰자 객체를 등록한다
			// chatAccess에서 notify가 발생할 경우 ChatFrame 객체의 update메소드가 실행된다.
			chatAccess.addObserver(this);
			// 구체적인 UI를 배치한다
			buildGUI();
		}

		/** Builds the user interface */
		private void buildGUI() {
			textArea = new JTextArea(20, 50);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			add(new JScrollPane(textArea), BorderLayout.CENTER);

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.SOUTH);
			inputTextField = new JTextField();
			sendButton = new JButton("Send");
			box.add(inputTextField);
			box.add(sendButton);

			// Action for the inputTextField and the goButton
			ActionListener sendListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String str = inputTextField.getText();
					if (str != null && str.trim().length() > 0)
						chatAccess.send(str);
					inputTextField.selectAll();
					inputTextField.requestFocus();
					inputTextField.setText("");
				}
			};
			inputTextField.addActionListener(sendListener);
			sendButton.addActionListener(sendListener);

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					chatAccess.close();
				}
			});
		}

		/** Updates the UI depending on the Object argument */
		// 관찰대상이 알림이 올 경우 실행되는 메소드
		@Override
		public void update(Observable o, Object arg) {
			final Object finalArg = arg;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// textArea라는 영역에 text를 출력한다.
					// 메시지를 보내는 경우, 받는 경우 모두 해당된다.
					textArea.append(finalArg.toString());
					textArea.append("\n");
				}
			});
		}
	}

	/**
	 * main class에서는 ChatAccess를 생성한다 생성한 ChatAccess object로 ChatFrame 를 생성한다
	 * */
	public static void main(String[] args) {
		String server = args[0];
		int port = 2222;
		ChatAccess access = null;
		try {
			access = new ChatAccess(server, port);
		} catch (IOException ex) {
			System.out.println("Cannot connect to " + server + ":" + port);
			ex.printStackTrace();
			System.exit(0);
		}
		JFrame frame = new ChatFrame(access);
		frame.setTitle("MyChatApp - connected to " + server + ":" + port);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}