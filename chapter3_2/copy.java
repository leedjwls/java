package chapter3;
import java.io.*;
import java.util.Scanner;

public class copy {
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("파일복제");
		System.out.print("복사할 파일이름: ");
		String name1 = scan.nextLine();
		System.out.print("저장할 파일이름: ");
		String name2 = scan.nextLine();
		File f = new File (name1);
		long fsize = f.length();
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		
		try {
			inputStream = new FileInputStream(f);
			outputStream = new FileOutputStream(name2);
			int input = 0, count = 0, idx = 0;
			byte[] size = new byte[200];
			int[] ing = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100}; // 진행률 10%마다 확인
			  
			while((input=inputStream.read(size)) != -1) {
				outputStream.write(size, 0, input);
				count += input;
				float per = ((float)count/fsize) * 100;
				  
				if((int)per % 10 == 0) { // 진행률 10%마다
					if(ing[idx] == (int)per) {
						System.out.print("*"); // *로 표시
						idx ++;
					}
				}
			}
		}
		catch(Exception e) {
			System.err.println(e.toString());
		}
		finally {
			try {
				if(inputStream != null) inputStream.close();
				if(outputStream != null) outputStream.close();
			}
			catch (IOException e2) {
			}
		}
	}
}