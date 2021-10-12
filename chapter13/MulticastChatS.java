package chapter13;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class MulticastChatS extends Frame {
	TextArea display;
	Label info;
	String group = "239.10.1.1";
	DatagramPacket outgoing, ingoing, mpacket;
	int port = 6000;
	boolean logon = false;

	private static final String SEPARATOR = "|";
	private static final int REQ_LOGON = 1001;
	private static final int REQ_LOGOUT = 1002;
	private static final int REQ_SENDWORDS = 1021;

	public MulticastChatS() {
		super("서버");
		info = new Label();
		add(info, BorderLayout.NORTH);
		display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		display.setEditable(false);
		add(display, BorderLayout.CENTER);
		addWindowListener(new WinListener());

		setSize(350, 300);
		setVisible(true);
	}

	public void runServer() {
		DatagramSocket socket = null;
		MulticastSocket server = null;
		outgoing = new DatagramPacket(new byte[1], 1);
		ingoing = new DatagramPacket(new byte[65508], 65508);

		try {
			socket = new DatagramSocket(6000);
			while (true) {
				ingoing.setLength(ingoing.getData().length);
				socket.receive(ingoing);
				String message = new String(ingoing.getData(), 0, ingoing.getLength());
				StringTokenizer st = new StringTokenizer(message, SEPARATOR); //"|"이전의 message를 토큰으로 분리
				int command = Integer.parseInt(st.nextToken());

				switch (command) {
				case REQ_LOGON: { //"1001|아이디"를 수신한 경우
					String ID = st.nextToken();
					display.append("클라이언트가 " + ID + "(으)로 로그인 하였습니다.\r\n");
					InetAddress client_addr = ingoing.getAddress();
					int client_port = ingoing.getPort();
					String group_addr = group + "|" + port;
					byte[] buf = group_addr.getBytes();
					outgoing.setData(buf);
					outgoing.setLength(buf.length);
					outgoing.setAddress(client_addr);
					outgoing.setPort(client_port);

					socket.send(outgoing);
					logon = true;
					server = new MulticastSocket(15);
					break;
				}

				case REQ_LOGOUT: {
					String ID = st.nextToken();
					display.append("클라이언트 " + ID + "(이)가 로그아웃 하였습니다.\r\n");
					logon = false;
					break;
				}

				case REQ_SENDWORDS: { //"1021|아이디|대화말"을 수신한 경우
					String ID = st.nextToken();
					String text = st.nextToken();
					display.append(ID + " : " + text + "\r\n");
					String rcv = ID + " : " + text + "\r";
					byte[] buf = rcv.getBytes("UTF8");
					outgoing = new DatagramPacket(buf, buf.length, InetAddress.getByName(group), 15);
					server.send(outgoing);
					break;
				}

				}
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void main(String args[]) throws IOException {
		MulticastChatS s = new MulticastChatS();
		s.runServer();
	}

	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
}