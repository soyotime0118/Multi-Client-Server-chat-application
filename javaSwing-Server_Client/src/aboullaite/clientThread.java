/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aboullaite;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author mohammed
 */

// For every client's connection we call this class
// 모든 client의 연결은 이 클래스를 호출한다.
public class clientThread extends Thread {
	// client의 대화명
	private String clientName = null;
	private DataInputStream is = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private int maxClientsCount;

	/**
	 * 
	 * @param clientSocket
	 *            채팅 text 전송에 사용할 clientSocket
	 * @param threads
	 *            다른 clientThread가 모여있는 thread 배열
	 */
	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
	}

	public void run() {
		int maxClientsCount = this.maxClientsCount;
		clientThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client. 이 client에서 사용할
			 * 입력 출력 stream들을 만든다
			 */
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			String name;
			while (true) {
				// 이름 입력
				os.println("Enter your name.");
				// 공칸 제거
				name = is.readLine().trim();
				if (name.indexOf('@') == -1) {
					break;
				} else {
					os.println("The name should not contain '@' character.");
				}
			}

			/* Welcome the new the client. */
			// 사용자의 생성을 알림
			os.println("Welcome " + name
					+ " to our chat room.\nTo leave enter /quit in a new line.");
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this) {
						// 모든 client에 새로운 사용자가 채팅방에 생성되었음을 알린다
						threads[i].os.println("*** A new user " + name
								+ " entered the chat room !!! ***");
					}
				}
			}
			/* Start the conversation. */
			while (true) {
				String line = is.readLine();
				// 종료 명령어quit
				if (line.startsWith("/quit")) {
					break;
				}
				/* If the message is private sent it to the given client. */
				// 귓속말 기능
				if (line.startsWith("@")) {
					String[] words = line.split("\\s", 2);
					if (words.length > 1 && words[1] != null) {
						words[1] = words[1].trim();
						if (!words[1].isEmpty()) {
							synchronized (this) {
								for (int i = 0; i < maxClientsCount; i++) {
									// 사용자의 개념이 thread이므로, 귓속말을 받는 사용자thread를
									// 찾아낸다.
									if (threads[i] != null
											&& threads[i] != this
											&& threads[i].clientName != null
											&& threads[i].clientName
													.equals(words[0])) {
										threads[i].os.println("<" + name + "> "
												+ words[1]);
										/*
										 * Echo this message to let the client
										 * know the private message was sent.
										 */
										// 귓속말 기능은 보낸사용자, 받는 사용자가 동시에 봐야 하므로 보낸
										// 사용자thread에게도 보내진다
										this.os.println(">" + name + "> "
												+ words[1]);
										break;
									}
								}
							}
						}
					}
				} else {
					/* The message is public, broadcast it to all other clients. */
					// 귓속말이 아니라면, 모든 사용자에게 다 보내진다
					synchronized (this) {
						for (int i = 0; i < maxClientsCount; i++) {
							if (threads[i] != null
									&& threads[i].clientName != null) {
								threads[i].os.println("<" + name + "> " + line);
							}
						}
					}
				}
				// 무한 루프이므로, 사용자가 종료 명령을 내리기 않았다면 여기까지 무조건 실행된다.
			}
			synchronized (this) {
				// 사용자가 채팅방을 나갔을 경우, 채팅방을 나갔다는 메시지를 출력한다
				// 여기서부터는 사용자가 채팅방을 나갔을 경우에 발생하는 작업들이다
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] != this
							&& threads[i].clientName != null) {
						threads[i].os.println("*** The user " + name
								+ " is leaving the chat room !!! ***");
					}
				}
			}
			os.println("*** Bye " + name + " ***");

			/*
			 * Clean up. Set the current thread variable to null so that a new
			 * client could be accepted by the server. 현재 thread변수를 null로 설정한다
			 */
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the
			 * socket. socket, inputStream과 outputStream을 모두 닫는다
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}
}
