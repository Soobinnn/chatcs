package main.java.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ChatServerThread extends Thread
{
	
	private String nickname;
	private Socket socket;
	private List<Writer> listWriters;
	BufferedReader bufferedReader = null;
	PrintWriter printWriter = null;
	
	public ChatServerThread(Socket socket)
	{
		this.socket = socket;
	}
	
	public ChatServerThread(Socket socket, List<Writer> listWriters)
	{
		this.socket = socket;
		this.listWriters = listWriters;
	}
	
	@Override
	public void run()
	{
		try
		{
			// 1. Remote Host Information
			InetSocketAddress inetSocketAddress = ( InetSocketAddress )socket.getRemoteSocketAddress();
			ChatServer.log( "connected from " + inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort() );
			
			// 2. 스트림 얻기
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
			
			printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"), true);
			
			// 3. 요청 처리
			while(true)
			{
				String request = bufferedReader.readLine();
				if(request == null)
				{
					ChatServer.log("클라이언트로부터 연결 끊김");
					doQuit(printWriter);
					break;
				}
				
				ChatServer.log(" [received]" + request);
				
				// 4. 프로토콜 분석
				String[] tokens = request.split(":");
				
				if("join".equals(tokens[0]))
				{
					doJoin(tokens[1], printWriter);
				}
				else if("message".equals(tokens[0]))
				{
					doMessage(tokens[1]);
				}
				else if("quit".equals(tokens[0]))
				{
					doQuit(printWriter);
				}
				else
				{
					ChatServer.log("에러: 알수 없는 요청(" +tokens[0]+")");
				}
				
			}
		}
		catch(SocketException e)
		{
			System.out.println("[server] sudden closed by client");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(socket != null && socket.isClosed() == false)
				{
					socket.close();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	// join 프로토콜
	private void doJoin(String nickName, Writer writer)
	{
		this.nickname = nickName;
		
		String data = "["+nickName+"]" + "님이 참여하였습니다.";
		broadcast(data);
		
		// writer pool에 저장
		addWriter(writer);
		
		ChatServer.log(nickName+"님 입장~");
		//ack
		printWriter.println("join:ok");
		printWriter.flush();
	}
	
	// message 프로토콜
	private void doMessage(String message)
	{
		//구현하기
		String msg = "["+nickname+"]: " +message;
		broadcast(msg);
	}
	
	// quit 프로토콜 
	private void doQuit(Writer writer)
	{
		removeWriter(writer);
		
		String data = "["+nickname+"]" + "님이 퇴장 하였습니다.";
		broadcast(data);
	}
	
	private void addWriter(Writer writer)
	{
		synchronized(listWriters)
		{
			listWriters.add(writer);
		}
		listCount();
	}

	private void broadcast(String data)
	{
		synchronized(listWriters)
		{
			for(Writer writer : listWriters)
			{
				PrintWriter _printWriter = (PrintWriter)writer;
				
				if(printWriter.equals(_printWriter))
				{
					continue;
				}
				_printWriter.println(data);
				_printWriter.flush();
			}
		}
	}
	
	private void removeWriter(Writer writer)
	{
		listWriters.remove(writer);
		listCount();
	}
	
	// [테스트] 인원체크용
	private void listCount()
	{
		int count = 0;
		synchronized(listWriters)
		{
			for(Writer writer : listWriters)
			{
				count++;
			}
		}
		ChatServer.log("[현재접속인원] "+count+"명");
	}
}

