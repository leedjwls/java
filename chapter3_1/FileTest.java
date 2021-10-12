package chapter3;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileTest extends Frame implements ActionListener {
	private TextField enter;
	private TextArea output1, output2;
	
	public FileTest() {
		super("FileTest");
		
		enter = new TextField("파일 및 디렉토리명을 입력하세요");
		enter.addActionListener(this);
		
		output1 = new TextArea();
		output2  = new TextArea();
		add(enter, BorderLayout.NORTH);
		add(output1, BorderLayout.CENTER);
		add(output2, BorderLayout.SOUTH);
		addWindowListener(new WinListener());
		setSize(400, 400);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		File name = new File(e.getActionCommand()); // 텍스트 필드의 파일이름을 읽음;
		Long lastModified = name.lastModified();
		SimpleDateFormat simpledate = new SimpleDateFormat("yyyy년 MM월 dd일 (E요일) hh시 mm분", Locale.KOREA);
		String fileDate = simpledate.format(lastModified);
		
		if(name.exists()) {
			output1.setText(name.getName() + "이 존재한다.\n" + (name.isFile() ? "파일이다.\n" : "파일이 아니다.\n") +
					(name.isDirectory() ? "디렉토리이다.\n" : "디렉토리가 아니다.\n") +
					(name.isAbsolute() ? "절대경로이다.\n" : "절대경로가 아니다.\n") +
					"마지막 수정날짜는 : " + fileDate + "\n파일의 길이는 : " + name.length()); 
			try {
				output2.setText("파일의 경로는 : " + name.getPath() + "\n절대경로는 : " +
						name.getAbsolutePath() + "\n정규경로는 : " + name.getCanonicalPath() + "\n상위 디렉토리는 : " + name.getParent()); 
			} catch (IOException e3) {
				System.err.println(e3.toString());
			}
			
			if(name.isFile()) {
				try {
					RandomAccessFile r = new RandomAccessFile(name, "r");
					StringBuffer buf = new StringBuffer();
					String text;
					output2.append("\n\n");
					while((text = r.readLine()) != null)
						buf.append(text + "\n");
					output2.append(buf.toString());
				} catch (IOException e2) {
				}
			}
			else if(name.isDirectory()) {
				String directory[] = name.list();
				output2.append("\n\n디렉토리의 내용은 :\n");
				for(int i=0; i<directory.length; i++)
					output2.append(directory[i] + "\n");
			}
		}
		else {
			output1.setText(e.getActionCommand() + " 은 존재하지 않는다\n");
		}
	}
	public static void main(String[] args) {
		FileTest f = new FileTest();
	}
	class WinListener extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			System.exit(0);
		}
	}
}