package aboullaite;

import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/**
 *
 * @author mohammed
 */

// the Server class
public class MultiThreadChatServerSync {
	// 서버 소켓 선언
	// The server socket.
	private static ServerSocket serverSocket = null;
	// 클라이언트 소켓 선언
	// The client socket.
	private static Socket clientSocket = null;

	// 이 채팅 서버는 maxClientsCount만큼 접속이 가능하다.
	private static final int maxClientsCount = 10;
	// client thread를 생성
	private static final clientThread[] threads = new clientThread[maxClientsCount];

	public static void main(String args[]) {

		// The default port number.
		// 기본 포트
		int portNumber = 2222;
		if (args.length < 1) {
			System.out
					.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
							+ "Now using port number=" + portNumber);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
		}

		/*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can 지정된 포트로 서버 소켓을 생성한다. 1023보다 아래의 포트를 사용하기 위해서는 관리자 권한이 필요하다. not
		 * choose a port less than 1023 if we are not privileged users (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a client socket for each connection and pass it to a new
		 * client thread. 각각의 연결connection 생성을 위한 client socket을 만들고, 생성한 client
		 * socket를 client thread에 전달한다.
		 */
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						// 클라이언트 thread 시작
						(threads[i] = new clientThread(clientSocket, threads))
								.start();
						break;
					}
				}
				// client thread가 매번 실행 될 때마다 maxClientsCount를 넘었는지 확인
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(
							clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}
