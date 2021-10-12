package chapter12;
import java.io.*;
import java.net.*;
import java.util.*;

public class GetMIMEHeader {
	public static void main(String args[]) throws IOException {
		URL u;
		URLConnection uc;
		Scanner scan = new Scanner(System.in);
		System.out.print("URL을 입력하세요 : ");
		String text = scan.nextLine();
		System.out.print("file명을 입력하세요 : ");
		String file = scan.nextLine();
		
		try {
			u = new URL(text); // URL객체 생성
			uc = u.openConnection();
				FileOutputStream fw = new FileOutputStream(".\\download12\\" + file + ".txt");
				OutputStreamWriter sr = new OutputStreamWriter(fw);
				BufferedWriter bw = new BufferedWriter(sr);
				System.out.println("컨텐트 유형 : " + uc.getContentType());
				System.out.println("컨텐트 인코딩 : " + uc.getContentEncoding());
				System.out.println("문서전송날짜 : " + new Date(uc.getDate()));
				System.out.println("최종수정날짜 : " + new Date(uc.getLastModified()));
				System.out.println("문서만기날짜 : " + new Date(uc.getExpiration()));
				System.out.println("문서길이 : " + uc.getContentLength());
				
		} catch(MalformedURLException e) {
			System.out.println("입력된 URL은 잘못된 URL입니다");
		} catch(IOException e) {
			System.out.println(e);
		}
	}
}
