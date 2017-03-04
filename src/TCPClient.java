import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImageFilter;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.text.DefaultCaret;

import java.net.*;
import java.io.*;

//this class can make changes to ChattingWindow's elements
public class TCPClient
{
	private ChattingWindow referenceToChattingWindow;
	private Socket socket;
	private DataOutputStream dataOutputStream;
	private DataInputStream dataInputstream;
	private String userName="";
	private String serverIP="";
	
	public TCPClient(ChattingWindow referenceToChattingWindow,String serverIP,String userName){
		this.userName=userName;
		this.referenceToChattingWindow=referenceToChattingWindow;
		this.serverIP=serverIP;
	}
	
	//too much work
	//must not call this function in constructor
	public void startTCPClient(){
		try {
			socket=new Socket(serverIP,2222);
			dataOutputStream=new DataOutputStream(socket.getOutputStream());
			dataInputstream=new DataInputStream(socket.getInputStream());
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
					+ "\n(1)If server is running"
					+ "\n(2)If you and server are on the same network "
					+ "\nPlease start the application again.","ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		//sending name of the client as first message
		try {
			dataOutputStream.writeUTF(userName);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
					+ "\n(1)If server is running"
					+ "\n(2)If you and server are on the same network "
					+ "\nPlease start the application again.","ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		//recieve the first message which will be the name of online clients bundled in one string
		//names are separated by '+' character in string
		
		String userNamesOfActiveClients="";
		try {
			userNamesOfActiveClients=dataInputstream.readUTF();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
					+ "\n(1)If server is running"
					+ "\n(2)If you and server are on the same network "
					+ "\nPlease start the application again.","ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		
		//populating list of Active clients in ChattingWindow
		while(!userNamesOfActiveClients.equals("")){
			referenceToChattingWindow.listModelForClientName.addElement(userNamesOfActiveClients.substring(0, userNamesOfActiveClients.indexOf("+")));
			userNamesOfActiveClients=userNamesOfActiveClients.substring(userNamesOfActiveClients.indexOf("+")+1);
		}
		
		//starting new thread to wait for messages
		new Thread(new Runnable() {
			String message;
			@Override
			public void run() {
				while(true){

					try{
						message=dataInputstream.readUTF();
						System.out.println(message);
					}catch(IOException e){
						JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
								+ "\n(1)If server is running"
								+ "\n(2)If you and server are on the same network "
								+ "\nPlease start the application again.","ERROR",JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
					if(message.trim().startsWith("INSERT@")){
						referenceToChattingWindow.listModelForClientName.addElement(message.substring(message.indexOf('@')+1));
					}
					else if(message.trim().startsWith("REMOVE@")){
						referenceToChattingWindow.listModelForClientName.remove(referenceToChattingWindow.listModelForClientName.indexOf(message.substring(message.indexOf('@')+1)));
					}
					else
						referenceToChattingWindow.textAreaIncomingMessages.append(message);
				}
			}
		}).start();

	}
	
	public void sendMessage(String message){
		try {
			dataOutputStream.writeUTF(message);
		} catch (IOException e) {

		}
	}

}

