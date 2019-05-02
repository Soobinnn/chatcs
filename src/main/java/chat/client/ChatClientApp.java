package main.java.chat.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp 
{
	private static final String SERVER_IP = "192.168.56.1";
	private static final int SERVER_PORT = 7000;
	
	public static void main(String[] args) 
	{
		Socket socket = null;
		String name = null;
		Scanner scanner = new Scanner(System.in);
		
		
		while( true ) 
		{
			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();
			
			if (name.isEmpty() == false) 
			{
				break;
			}
			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		
		try 
		{
			// 1. 소켓 생성하기
			socket = new Socket();
			
			// 2. join 프로토콜
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			log("connect :" + name);
			
			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"), true);
			printWriter.println("join:" + name);
			
			new ChatWindow(name, socket, printWriter).show();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		scanner.close();

	}
	
	public static void log(String log)
	{
		System.out.println("[client] "+log);
	}

}
