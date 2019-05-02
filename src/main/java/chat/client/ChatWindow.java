package main.java.chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ChatWindow 
{

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket = null;
	private PrintWriter printWriter = null;
	
	public ChatWindow(String name, Socket socket, PrintWriter printWriter) 
	{
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		this.socket = socket;
		this.printWriter = printWriter;
		
		new ChatClientReceiveThread(socket).start();
	}

	public void show() 
	{
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() 
		{
			@Override
			public void actionPerformed( ActionEvent actionEvent ) 
			{
				sendMessage();
			}
		});
				
		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_ENTER)
				{
					sendMessage();
				}
			}
		});
		
		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
	}
	
	
	private void updateTextArea(String message)
	{
		textArea.append(message);
		textArea.append("\n");
	}
	
	private void sendMessage() 
	{
		String message = textField.getText();
		//pw.println("MSG " + message);
		textField.setText("");
		textField.requestFocus();
		updateTextArea(message);
		
		if("quit".equals(message)) 
		{
			finish();
		}
		else
		{
			printWriter.println("message:"+message);
		}
	}
	
	private void finish()
	{
		System.out.println(".............");
		printWriter.println("quit:");
		System.exit(0);
	}
	private class ChatClientReceiveThread extends Thread
	{
		Socket socket = null;
		
		ChatClientReceiveThread(Socket socket)
		{
			this.socket = socket;
		}
		
		@Override
		public void run() 
		{
			try 
			{
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
				while(true) 
				{
					String message = bufferedReader.readLine();
					updateTextArea(message);
				}
			}
			catch(SocketException e)
			{
				
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
}

