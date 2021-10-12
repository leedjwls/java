package chapter6;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageProducer;
import java.net.*;
import java.io.*;

public class ReadServerFile extends Frame implements ActionListener {
	private TextField enter;
	private TextArea contents, contents2;
	public ReadServerFile() {
		super("호스트 파일 읽기");
		setLayout( new BorderLayout() );
		enter = new TextField( "URL를 입력하세요!" );
		enter.addActionListener( this );
		add( enter, BorderLayout.NORTH );
		
		contents = new TextArea("");
		add(contents, BorderLayout.CENTER);
		
		contents2 = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		add(contents2, BorderLayout.SOUTH);
		addWindowListener(new WinListener());
		setSize(350, 350);
		setVisible(true);
	}
	public void actionPerformed( ActionEvent e ) {
		URL url;
		InputStream is;
		BufferedReader input;
		String line, line2, type;
		StringBuffer buffer = new StringBuffer();
		String location = e.getActionCommand(); // 텍스트 필드에 입력된 URL를 구함
		Object object;
		try {
			url = new URL( location );
			is = url.openStream(); // location(호스트)과 연결시키는 InputStream 객체생성
			input = new BufferedReader(new InputStreamReader(is));
			object = url.getContent();
			URLConnection urlc = url.openConnection();
			urlc.connect();
			type = urlc.getContentType();
			
			line2 = "protocol: " + url.getProtocol() + "\n";
			contents.append(line2);
			line2 = "host name: " + url.getHost() + "\n";
			contents.append(line2);
			line2 = "port no: " + url.getPort() + "\n";
			contents.append(line2);
			line2 = "file name: " + url.getFile() + "\n";
			contents.append(line2);
			line2 = "hash code: " + url.hashCode() + "\n";
			contents.append(line2);
			
			contents2.setText( "파일을 읽는 중입니다...." );
			
			if(object instanceof ImageProducer)
				contents2.setText("image");
			else if(type.contains("audio"))
				contents2.setText("audio");
			else if(type.contains("video"))
				contents2.setText("video");
			else if(object instanceof InputStream) {
				while ( ( line = input.readLine() ) != null ) // 파일(웹페이지)을 읽는다.
		            buffer.append( line ).append( '\n' );
					contents2.setText( buffer.toString() ); // 읽은 파일을 텍스트 에리어에 출력
			}
			input.close();
			
		} catch(MalformedURLException mal) {
			contents2.setText("URL 형식이 잘못되었습니다.");
		} catch ( IOException io ) {
			contents2.setText( io.toString() );
		} catch ( Exception ex ) {
			contents2.setText( "호스트 컴퓨터의 파일만을 열 수 있습니다." );
		}
	}
	public static void main(String args[]) {
		ReadServerFile read = new ReadServerFile();
	}
	class WinListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent we) {
			System.exit(0);
		}
	}
}