package chapter7;
import java.io.*;
import java.net.*;
import java.util.*;

public class WordClient {
	public static void main(String args[]) {
		BufferedWriter bw;
		BufferedReader br;
		InputStream is;
		OutputStream os;
		Socket theSocket;
		String host, KOR, ENG;
		while(true) {
			Scanner scan = new Scanner(System.in);
			System.out.print("영어 단어를 입력하세요: ");
			ENG = scan.nextLine();
			
			if(args.length > 0) {
				host = args[0];
			}
			else {
				host = "localhost";
			}
			try {
				theSocket = new Socket(host, 13);
				os = theSocket.getOutputStream();
				bw = new BufferedWriter(new OutputStreamWriter(os));
				bw.write(ENG + "\r\n");
				bw.flush();
			
				is = theSocket.getInputStream();
				br = new BufferedReader(new InputStreamReader(is));
				KOR = br.readLine();
				System.out.println(KOR);
			} catch(UnknownHostException e) {
				System.err.println(args[0] + "을 찾을 수 없습니다");
			} catch(IOException e) {
			System.err.println(e);
			}
		}
	}
}
