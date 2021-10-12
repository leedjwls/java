package chapter4;
import java.io.*;
import java.util.*;

public class Buffer {
	public static void main(String args[]) throws IOException {
		Scanner scan = new Scanner(System.in);
		FileWriter fw = new FileWriter("example4_12.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		String text;
		int num = 1;
		
		while(true) {
			text = scan.nextLine();
			
			if(text.equals("leave"))
				break;
			
			bw.write(num + ":" + text + "\n");
			bw.flush();
			num++;
		}
		
		try
		{
			FileReader fr = new FileReader("example4_12.txt");
			BufferedReader br = new BufferedReader(fr);
		
			while((text = br.readLine()) != null)
			{
				System.out.println(text);
			}	
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}
