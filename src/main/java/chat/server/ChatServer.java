package chat.server;

import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer 
{
private static final int PORT = 7000;
	
	public static void main(String[] args) 
	{
		List<Writer> listWriters = new ArrayList<Writer>();
		
		ServerSocket serverSocket = null;
		try 
		{
			// 1. 서버 소켓 생성
			serverSocket = new ServerSocket();
				
			// 2. 바인딩
			//String hostAddress = InetAddress.getLocalHost().getHostAddress();
			serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
			//log("연결 기다림 " + hostAddress + ":" + PORT);
			log("연결 기다림 :" + PORT);
				
			// 3. 요청 대기
			while(true)
			{
				Socket socket = serverSocket.accept();
				
				//new ChatServerThread(socket).start();
				new ChatServerThread(socket, listWriters).start();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				if(serverSocket != null && serverSocket.isClosed() == false)
				{
					serverSocket.close();	
				}
			} 
			catch (IOException e) 
			{
			
				e.printStackTrace();
			}
		}
	}
	public static void log(String log)
	{
		System.out.println("[server#" + Thread.currentThread().getId() +" "+log);
	}
}
