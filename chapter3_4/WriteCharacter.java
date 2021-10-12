package chapter4;
import java.io.*;
import java.util.Scanner;

public class WriteCharacter {
	public static void main(String args[]) throws IOException {
		int numberRead;
		String data;
		char[] buffer = new char[80];
		Scanner scan = new Scanner(System.in);
		System.out.print("내용을 입력하세요: ");
		String text = scan.nextLine();
		System.out.println("파일내용: ");
		FileWriter fw = new FileWriter("example4_1.txt"); // 디폴트(유니코드) 인코딩 방식을 사용하여 저장한다
		fw.write(text, 0, text.length());
		fw.flush();
		fw.close();
		FileReader fr = new FileReader("example4_1.txt");
		while((numberRead = fr.read(buffer)) > -1) {
			System.out.println(buffer);
		}
		fr.close();
	}
}
