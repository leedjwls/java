package chapter1;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class chapter1_1 {
	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);
		
		while(true) {
			try {
				System.out.print("파일 이름을 입력하세요: ");
				
				String name = scan.next(); // 보고자 하는 파일의 이름을 입력받는
				
				FileInputStream in = null;
				
				try {
					System.out.println("파일 이름: " + name);
					System.out.print("파일 내용: ");
					int i = 0;
					in = new FileInputStream(name);
					while((i = in.read()) != -1)
						System.out.write(i); // 파일의 내용을 출력
					System.out.println();
				}
				catch(FileNotFoundException e) {
					System.out.println("파일 이름을 정확히 입력해주세요");
				}
				catch(IOException e) {
					System.out.println(e.getMessage());
				}
				finally {
					try {
						if(in != null)
							in.close();
					}
					catch(IOException io) {
						
					}
				}
			}
			catch(NoSuchElementException e) {
				System.out.println("종료");
				break;
			}
		}
	}
}
