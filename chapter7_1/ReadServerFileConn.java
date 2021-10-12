package chapter12;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.net.URLConnection;
import java.io.*;

public class ReadServerFileConn extends Frame implements ActionListener {
	private TextField enter;
	private TextArea contents;
	public ReadServerFileConn() {
		super("호스트 파일 읽기");
		setLayout(new BorderLayout());
		enter = new TextField("URL을 입력하세요");
		enter.addActionListener(this);
		add(enter, BorderLayout.NORTH);
		contents = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		add(contents, BorderLayout.CENTER);
		addWindowListener(new WinListener());
		setSize(350, 400);
		setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		URL url;
		URLConnection urlconn;
		InputStream is;
		BufferedReader br;
		String line;
		StringBuffer buffer = new StringBuffer();
		String location = e.getActionCommand();
		try {
			url = new URL(location);
			urlconn = url.openConnection();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			String headertype = urlconn.getContentType();
			is = urlconn.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			contents.setText("파일을 읽는 중입니다...");
			line = "헤더타입 : " + headertype;
			buffer.append(line + "\n");
			buffer.append("응답코드 : " + con.getResponseCode() + "\n");
			buffer.append("응답구문 : " + con.getHeaderFields() + "\n\n");
			
			while((line = br.readLine()) != null)
				buffer.append(line).append('\n');
			contents.setText(buffer.toString());
			br.close();
		} catch(MalformedURLException mal) {
			contents.setText("URL 형식이 잘못되었습니다");
		} catch(IOException io) {
			contents.setText(io.toString());
		} catch(Exception ex) {
			contents.setText("호스트 컴퓨터의 파일만을 열 수 있습니다");
		}
	}
	public static void main(String args[]) {
		ReadServerFileConn read = new ReadServerFileConn();
	}
	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			System.exit(0);
		}
	}
}
