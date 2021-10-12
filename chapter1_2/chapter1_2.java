package chapter1;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
 
public class chapter1_2 extends Frame implements ActionListener
{
    Label labelfile, labeldata;
    TextField textfile, textdata; // 파일의 이름을 입력받을 텍스트필
    TextArea textarea;
    Button copy, print, exit; // 복사, 출력, 종료 버튼
    String filename1, filename2;
    byte buffer[] = new byte[80];
    
    public chapter1_2(String str)
    {
        super(str);
        this.setSize(300, 500);
        this.setLayout(new FlowLayout());
        
        labelfile = new Label("입력파일");
        add(labelfile);
        
        textfile = new TextField(20); // 복사할 파일의 이름을 입력하기위한 TextField
        add(textfile);
        
        copy = new Button("복사"); // 복사 버튼
        copy.addActionListener(this);
        add(copy); // 복사 버튼 추가
        
        labeldata = new Label("출력파일");
        add(labeldata);
        
        textdata = new TextField(20); // 출력할 파일의 이름을 입력하기위한 TextField
        add(textdata);
        
        print = new Button("출력"); // 출력 버튼
        print.addActionListener(this);
        add(print); // 출력 버튼 추가
        
        textarea = new TextArea(10, 35); // 파일의 복사와 출력이 완료되면 해당 파일의 내용을 보여줄 TextArea
        add(textarea);
        addWindowListener(new WinListener());
        
        exit = new Button("닫기"); // 닫기 버튼
        exit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
        add(exit); // 닫기 버튼 추가
        
        addWindowListener(new WinListener());
    }
    
    public static void main(String[] args)
    {
        chapter1_2 text = new chapter1_2("파일 복사/출력");
        text.setSize(350,350); // 창 크기를 350x350으로 고정
        text.setVisible(true);
    }
    public void actionPerformed(ActionEvent ae)
    {
        filename1 = textfile.getText(); // 복사할 파일의 이름
        filename2 = textdata.getText(); // 복사된 파일의 이름
        
        FileInputStream in = null;
        FileOutputStream out = null;
        
        try
        {
            int byteRead;
            byte[] buffer = new byte[256];
            
            if(ae.getSource() == copy)
            {
                in = new FileInputStream(filename1); // 첫번째 파일을 읽어와서
                out = new FileOutputStream(filename2); // 두번째 파일에 복사
                
                while((byteRead = in.read(buffer)) >= 0)
                    out.write(buffer, 0, byteRead);
                textarea.setText("복사 성공!");
            }
            
            if(ae.getSource() == print)
            {
                in = new FileInputStream(filename2); // 두번째 파일을 읽어와서
                in.read(buffer);
                String data = new String(buffer);
                textarea.setText(data+"\n"); // 내용을 출력
            }
        }
        catch(FileNotFoundException e) // 파일이 없을시 경고문구 출력
        {
            textarea.setText("그런 파일은 없습니다");
        }
        catch(IOException e)
        {
            System.out.println(e.toString());
        }
        finally
        {
            try
            {
                if (in != null) in.close();
                if (out != null) out.close();
            }catch(IOException e) {}
        }
    }
 
    class WinListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent we)
        {
            System.exit(0);
        }
    }
}