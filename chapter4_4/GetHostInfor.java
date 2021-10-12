package chapter5;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.applet.*;

public class GetHostInfor extends Frame implements ActionListener {
	TextField hostname, Class; // 호스트 이름을 입력받는 필드
	Button getinfor; // 입력된 호스트에 관한 IP 정보를 읽는 버튼
	TextArea display; // 구해진 IP에 관한 정보를 출력하는 필드
	public static void main(String args[]) {
		GetHostInfor host = new GetHostInfor("InetAddress 클래스");
		host.setVisible(true);
	}
	public GetHostInfor(String str) {
		super(str);
		addWindowListener(new WinListener());
		setLayout(new BorderLayout());
		Panel inputpanel = new Panel();
		inputpanel.setLayout(new BorderLayout());
		inputpanel.add("North", new Label("호스트 이름:"));
		hostname = new TextField("", 30);
		getinfor = new Button("호스트 정보 얻기");
		inputpanel.add("Center", hostname);
		inputpanel.add("South", getinfor);
		getinfor.addActionListener(this);
		add("North", inputpanel);
		
		Panel outputpanel = new Panel();
		outputpanel.setLayout(new BorderLayout());
		display = new TextArea("", 24, 30);
		display.setEditable(false);
		outputpanel.add("North", new Label("인터넷 주소"));
		outputpanel.add("Center", display);
		add("Center", outputpanel);
		
		Panel outputpanel2 = new Panel();
		outputpanel2.setLayout(new BorderLayout());
		outputpanel2.add("North", new Label("Class, hashcode"));
		Class = new TextField("", 30);
		outputpanel2.add("Center", Class);
		add("South", outputpanel2);
		setSize(270, 300);
		
	}
	public void actionPerformed(ActionEvent e) {
		String name = hostname.getText();
		try {
			InetAddress inet = InetAddress.getByName(name);
			String ip = inet.getHostName() + "\n모든 주소\n";
			display.append(ip);
			ip = inet.getHostAddress() + "\n";
			display.append(ip);
			
			InetAddress[] machine = InetAddress.getAllByName(name);
			for(InetAddress i : machine) {
				ip = i.getHostAddress() + "\n";
				display.append(ip);
			}
			Class.setText(ipClass(inet.getAddress()) + "\n" + name.hashCode());
			
		} catch(UnknownHostException ue) {
		String ip = name + ": 해당 호스트가 없습니다.\n";
		display.append(ip);
		}
	}
	static String ipClass(byte[] ip) {
		int highByte = 0xff & ip[0];
		return(highByte < 128) ? "A" : (highByte < 192) ? "B" : (highByte < 224) ? "C" : (highByte < 240) ? "D" : "E";
	}
	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			System.exit(0);
		}
	}
}
