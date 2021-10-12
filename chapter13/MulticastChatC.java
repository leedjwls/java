package chapter13;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;

public class MulticastChatC extends Frame implements ActionListener, KeyListener {

	TextArea display;
	TextField wtext, ltext;
	Label mlbl, wlbl, loglbl;
	Button close;
	DatagramSocket client;
	Panel plabel, logout;
	StringBuffer clientdata;
	String data;
	String ID;
	DatagramPacket outgoing, ingoing;
	MulticastSocket socket;
	InetAddress group;
	String hostname = "localhost";
	boolean logon = false;

	private static final String SEPARATOR = "|";
	private static final int REQ_LOGON = 1001;
	private static final int REQ_LOGOUT = 1002;
	private static final int REQ_SENDWORDS = 1021;

	public MulticastChatC() {
		super("클라이언트");

		mlbl = new Label("채팅 상태를 보여줍니다.");
		add(mlbl, BorderLayout.NORTH);

		display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		display.setEditable(false);
		add(display, BorderLayout.CENTER);

		Panel ptotal = new Panel(new BorderLayout());
		Panel pword = new Panel(new BorderLayout());
		wlbl = new Label("대화말");
		wtext = new TextField(30); // 전송할 데이터를 입력하는 필드
		wtext.addKeyListener(this); // 입력된 데이터를 송신하기 위한 이벤트 연결
		pword.add(wlbl, BorderLayout.WEST);
		pword.add(wtext, BorderLayout.CENTER);
		ptotal.add(pword, BorderLayout.NORTH);

		plabel = new Panel(new BorderLayout());
		loglbl = new Label("로그온");
		ltext = new TextField(30); // 전송할 데이터를 입력하는 필드
		ltext.addActionListener(this); // 입력된 데이터를 송신하기 위한 이벤트 연결
		plabel.add(loglbl, BorderLayout.WEST);
		plabel.add(ltext, BorderLayout.CENTER);
		ptotal.add(plabel, BorderLayout.CENTER);

		logout = new Panel(new BorderLayout());
		close = new Button("로그아웃");
		close.addActionListener(this);
		logout.add(close, BorderLayout.CENTER);
		ptotal.add(logout, BorderLayout.SOUTH);
		logout.setVisible(false);

		add(ptotal, BorderLayout.SOUTH);

		addWindowListener(new WinListener());
		setSize(350, 300);
		setVisible(true);
	}

	public static void main(String args[]) {
		MulticastChatC c = new MulticastChatC();
		try {
			c.runClient();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == close) {
			plabel.setVisible(true);
			logout.setVisible(false);
			plabel.removeAll();
			plabel.add(loglbl, BorderLayout.WEST);
			plabel.add(ltext, BorderLayout.EAST);
			plabel.validate();
			logon = false;

			clientdata.setLength(0);
			clientdata.append(REQ_LOGOUT);
			clientdata.append(SEPARATOR);
			clientdata.append(ID);
			

			byte[] buffer;
			try {
				buffer = new String(clientdata).getBytes("UTF8");
				client.send(outgoing);

				socket.leaveGroup(group);
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			display.append("로그아웃\n");
			ID = "";
			ltext.setVisible(true);

		} else {
			ID = ltext.getText();
			ltext.setText("");

			if (ID.equals("") != true) { // 로그인 성공시 텍스트출력
				mlbl.setText(ID + "(으)로 로그인 하였습니다.");

				try {
					data = REQ_LOGON + SEPARATOR + ID;
					byte[] buf = data.getBytes();
					outgoing.setData(buf);
					outgoing.setLength(buf.length);
					client.send(outgoing);
					ingoing.setLength(ingoing.getData().length);
					client.receive(ingoing);
					String message = new String(ingoing.getData());
					StringTokenizer st = new StringTokenizer(message, SEPARATOR);
					String ip = st.nextToken();

					group = InetAddress.getByName(ip);

					String port = st.nextToken();
					display.append("멀티캐스트 채팅 그룹 주소는 " + ip + ":" + port + "입니다.\r\n");

					int mp = 15;
					if (st.hasMoreTokens())
						mp = Integer.parseInt(port);
					socket = new MulticastSocket(mp);

					socket.setTimeToLive(1);
					socket.joinGroup(group);

					Recv_msg servert = new Recv_msg(socket);
					servert.start();

					logon = true;
					logout.setVisible(true);
					plabel.removeAll();

					plabel.add(close, BorderLayout.CENTER);
					plabel.validate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				mlbl.setText("다시 로그인 하세요!!!");
			}
		}

	}

	public void runClient() throws IOException {

		client = new DatagramSocket();
		ingoing = new DatagramPacket(new byte[65508], 65508);
		outgoing = new DatagramPacket(new byte[1], 1, InetAddress.getByName(hostname), 6000);
		mlbl.setText("멀티캐스트 채팅 서버에 가입 요청합니다!");
		System.out.println(logon);
	}

	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			display.append("로그아웃\n");
			ID = "";
			ltext.setVisible(true);
			System.exit(0);
		}
	}

	class Recv_msg extends Thread {
		MulticastSocket msocket;
		private static final String SEPARATOR = "|";

		public Recv_msg(MulticastSocket socket) {
			msocket = socket;

		}

		public void run() {

			while (true) {
				ingoing = new DatagramPacket(new byte[65508], 65508);
				try {
					msocket.receive(ingoing);
				} catch (IOException ioe) {

					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
				display.append(new String(ingoing.getData()) + "\r\n");
			}

		}
	}

	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
			String message = new String();
			message = wtext.getText();

			if (ID == null) {
				mlbl.setText("로그인 후 이용하세요!!!");
				wtext.setText("");
			} else {

				data = REQ_SENDWORDS + SEPARATOR + ID + SEPARATOR + message;
				byte[] buf = null;
				try {
					buf = new String(data).getBytes("UTF8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outgoing.setData(buf);
				outgoing.setLength(buf.length);
				try {
					client.send(outgoing);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				wtext.setText("");

			}
		}
	}

	public void keyReleased(KeyEvent ke) {
	}

	public void keyTyped(KeyEvent ke) {
	}

}