package chapter9;

// Step 4
// 클라이언트 간의 채팅에서 특정 클라이언트와의 귓속말 구현
// 다음의 형식으로 귓속말 전송
// /w 상대방아이디 대화말
// package Server;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.awt.*;
import java.awt.event.*;

public class ChatWhisperS extends Frame {
	TextArea display;
	Label info;
	List<Map.Entry<ServerThread, Boolean>> list;
	Hashtable<String, ServerThread> hash;
	public ServerThread SThread;

	public ChatWhisperS() {
		super("서버");
		info = new Label();
		add(info, BorderLayout.CENTER);
		display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		display.setEditable(false);
		add(display, BorderLayout.SOUTH);
		addWindowListener(new WinListener());
		setSize(350, 250);
		setVisible(true);
	}

	public void runServer() {
		ServerSocket server;
		Socket sock;
		ServerThread SThread;
		try {
			server = new ServerSocket(5000, 100);
			hash = new Hashtable<String, ServerThread>();
			list = new ArrayList<Map.Entry<ServerThread, Boolean>>();
			try {
				while (true) {
					sock = server.accept();
					SThread = new ServerThread(this, sock, display, info);
					SThread.start();
					info.setText(sock.getInetAddress().getHostName() + " 서버는 클라이언트와 연결됨");
				}
			} catch (IOException ioe) {
				server.close();
				ioe.printStackTrace();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public static void main(String args[]) {
		ChatWhisperS s = new ChatWhisperS();
		s.runServer();
	}

	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
}

class ServerThread extends Thread {
	Socket sock;
	BufferedWriter output;
	BufferedReader input;
	TextArea display;
	Label info;
	TextField text;
	String clientdata;
	String serverdata = "";
	ChatWhisperS cs;
	String[] id = new String[100];
	boolean check = false;

	private static final String SEPARATOR = "|";
	private static final int REQ_LOGON = 1001;
	private static final int REQ_LOGOUT = 1002;
	private static final int REQ_EXIT = 1003;
	private static final int REQ_SENDWORDS = 1021;
	private static final int REQ_WISPERSEND = 1022;
	private Set<String> id2;

	public ServerThread(ChatWhisperS c, Socket s, TextArea ta, Label l) {
		sock = s;
		display = ta;
		info = l;
		cs = c;
		try {
			input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void run() {
		try {
			cs.list.add(new AbstractMap.SimpleEntry<ServerThread, Boolean>(this, false));
			while ((clientdata = input.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(clientdata, SEPARATOR);
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case REQ_LOGON: {
					String ID = st.nextToken();
					id2 = cs.hash.keySet();
					id2.toArray(id);
					for (int i = 0; i < id.length; i++) {
						if (id[i] != null) {
							if (id[i].compareTo(ID) == 0)
								check = true;
						}
					}
					cs.hash.put(this.toString(), this);
					if (!check) {
						cs.hash.remove(this.toString());
						cs.hash.put(ID, this); // 해쉬 테이블에 아이디와 스레드를 저장한다
						for (int i = 0; i < cs.list.size(); i++) {
							if (cs.list.get(i).getKey().getName().compareTo(cs.hash.get(ID).getName()) == 0) {
								cs.list.get(i).setValue(true);
								ServerThread SThread1 = (ServerThread) cs.list.get(i).getKey();
								SThread1.output.write("로그인 성공!\r\n");
								SThread1.output.flush();
								display.append("'" + ID + "'가 로그인 했습니다.\r\n");
							}
						}
					} else {
						for (int i = 0; i < cs.list.size(); i++) {
							if (cs.list.get(i).getKey().getName()
									.compareTo(cs.hash.get(this.toString()).getName()) == 0) {
								ServerThread SThread1 = (ServerThread) cs.list.get(i).getKey();
								SThread1.output.write("ID 중복.\r\n");
								SThread1.output.flush();
								display.append("중복된 ID '" + ID + "'입니다.\r\n");
							}
						}
					}
					check = false;
					break;
				}
				case REQ_SENDWORDS: {
					String ID = st.nextToken();
					String message = st.nextToken();
					display.append(ID + " : " + message + "\r\n");
					for (int i = 0; i < cs.list.size(); i++) {
						if (cs.list.get(i).getValue()) {
							ServerThread SThread = (ServerThread) cs.list.get(i).getKey();
							SThread.output.write(ID + " : " + message + "\r\n");
							SThread.output.flush();
						}
					}
					break;
				}
				case REQ_WISPERSEND: {
					String ID = st.nextToken();
					String WID = st.nextToken();
					String message = st.nextToken();
					if (cs.hash.containsKey(WID) == true) {
						display.append(ID + " -> " + WID + " : " + message + "\r\n");
						ServerThread SThread = (ServerThread) cs.hash.get(ID);
						// 해쉬테이블에서 귓속말 메시지를 전송한 클라이언트의 스레드를 구함
						SThread.output.write(ID + " -> " + WID + " : " + message + "\r\n");
						// 귓속말 메시지를 전송한 클라이언트에 전송함
						SThread.output.flush();
						SThread = (ServerThread) cs.hash.get(WID);
						// 해쉬테이블에서 귓속말 메시지를 수신할 클라이언트의 스레드를 구함
						SThread.output.write(ID + " : " + message + "\r\n");
						// 귓속말 메시지를 수신할 클라이언트에 전송함
						SThread.output.flush();
					}
					else {
						display.append("존재하지 않는 ID입니다.\r\n");
						output.write("NoId\r\n");
						output.flush();
					}
					break;
				}
				case REQ_LOGOUT: {
					String ID = st.nextToken();
					display.append("'" + ID + "'가 로그아웃 했습니다.\r\n");
					cs.hash.remove(ID);
					for (int i = 0; i < cs.list.size(); i++) {
						if (cs.list.get(i).getValue()) {
							ServerThread SThread = (ServerThread) cs.list.get(i).getKey();
							SThread.output.write("'" + ID + "'가 로그아웃 했습니다.\r\n");
							SThread.output.flush();
						}
					}
					break;
				}
				case REQ_EXIT: {
					String ID = st.nextToken();
					if (ID != null) {
						for (int i = 0; i < cs.list.size(); i++) {
							if (cs.list.get(i).getKey().getName().compareTo(cs.hash.get(ID).getName()) == 0) {
								display.append("'" + ID + "'가 로그아웃 했습니다.\r\n");
								cs.hash.remove(ID);
								cs.list.remove(i);
								break;
							}
						}
					} else {
						for (int i = 0; i < cs.list.size(); i++) {
							if (cs.list.get(i).getKey().getName()
									.compareTo(cs.hash.get(this.toString()).getName()) == 0) {
								display.append(cs.hash.get(this.toString()).getName());
								cs.hash.remove(cs.hash.get(this.toString()).getName());
								cs.list.remove(i);
								break;
							}
						}
					}
				}
				}
			}
		} catch (

		IOException e) {
			e.printStackTrace();
		}
		try {
			sock.close();
		} catch (IOException ea) {
			ea.printStackTrace();
		}
	}
}