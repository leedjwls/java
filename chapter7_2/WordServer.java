package chapter7;
import java.io.*;
import java.net.*;
import java.util.Date;

public class WordServer {
	public final static int daytimeport=13;
	public static void main(String args[]) {
		ServerSocket theServer;
		try {
			theServer = new ServerSocket(daytimeport);
			while(true) {
				Socket theSocket = null;
				WordServerDictionary client = null;
				try {
					theSocket = theServer.accept();
					client = new WordServerDictionary(theSocket);
					client.start();
				} catch(IOException e) {
					System.out.println(e);
				}
			}
		} catch(IOException e) {
			System.out.println(e);
		}
	}
}

class WordServerDictionary extends Thread {
	Socket connection;
	BufferedWriter bw;
	BufferedOutputStream os;
	InputStream is;
	BufferedReader br;
	
	String line, result2;

	public WordServerDictionary(Socket theSocket) {
		connection = theSocket;
	}
	
	public void run() {
		try {
			is = connection.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			line = br.readLine();
			
			result2 = match(line);
			
			os = new BufferedOutputStream(connection.getOutputStream());
			bw = new BufferedWriter(new OutputStreamWriter(os));
			bw.write(result2 + "\r\n");
			bw.flush();
		} catch(IOException e) {
			System.err.println(e);
		} finally {
			try {
				if(connection != null)
					connection.close();
				else
					connection.shutdownOutput();
			} catch(IOException e) {
				System.err.println(e);
			}
		}
	}
	public String match(String s) {
		String word, result = null;
		String[] dictionary = new String[6];
		
		try {
			FileReader fr = new FileReader("dictionary.txt");
			BufferedReader br = new BufferedReader(fr);
			word = br.readLine();
			int i = 0;
			
			while(word != null) {
			    dictionary[i] = word;
			    
				if(word.equals(s)) {
					result = dictionary[i-1];
					break;
				}	
				else
					result = "존재하지 않는 단어";
					i++;
			}
		} catch(IOException e) {
			System.err.println(e);
		}
		return result;
	}
}