package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ChatServerThread extends Thread
{
	// 1. Remote Host Information
	private String nickname;
	private Socket socket;
	List<Writer> listWriters;
	PrintWriter printWriter;
	
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
			// 2. 스트림 얻기
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
			
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
				
				ChatServer.log(" received : " + request);
				
				// 4. 프로토콜 분석
				String[] tokens = request.split(":");
				
				if("join".equals(tokens[0]))
				{
					doJoin(tokens[1], printWriter);
				}
				else if("message".equals(tokens[0]))
				{
					doMessage(tokens[1]);
					ChatServer.log("message옴");
				
				}
				else if("quit".equals(tokens[0]))
				{
					//doQuit();
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
			//doQuit(printWriter);
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
	/*
	catch(IOException e)
	{
		//에러처리
		if(request == null)
		{
			ChatServer.log("클라이언트로 부터 연결 끊김");
			doQuit(printWriter);
			break;
		}
	}
	*/
	
	// join 프로토콜
	private void doJoin(String nickName, Writer writer)
	{
		this.nickname = nickName;
		
		String data = nickName + "님이 참여하였습니다.";
		broadcast(data);
		
		// writer pool에 저장
		addWriter(writer);
		ChatServer.log(nickName+listWriters.toArray() +"님 입장~");
		//ack
		printWriter.println("join:ok");
		printWriter.flush();
	}
	
	private void addWriter(Writer writer)
	{
		synchronized(listWriters)
		{
			listWriters.add(writer);
		}
	}

	private void broadcast(String data)
	{
		synchronized(listWriters)
		{
			for(Writer writer : listWriters)
			{
				PrintWriter printWriter = (PrintWriter)writer;
				  ChatServer.log("현재접속인원1"+listWriters.get(0).toString());
				  ChatServer.log("현재접속인원2"+listWriters.get(1).toString());
				printWriter.println(data);
				printWriter.flush();
			}
		}
	}
	
	// message 프로토콜
	private void doMessage(String message)
	{
		//구현하기
		printWriter.println(message);
	}
	
	// quit 프로토콜 
	private void doQuit(Writer writer)
	{
		removeWriter(writer);
		
		String data = nickname + "님이 퇴장 하였습니다";
		broadcast(data);
	}
	
	private void removeWriter(Writer writer)
	{
		listWriters.remove(writer);
	}
	

}

