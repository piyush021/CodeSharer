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
	final private String directoryToStoreRecievedFiles="C:\\Users\\Varsha yadav\\Saurabh\\client2";
	public boolean isSendingFile=false;
	public boolean isRecievingFile=false;
	private String userName="";
	private String serverIP="";
	
	public TCPClient(ChattingWindow referenceToChattingWindow,String serverIP,String userName){

		this.userName=userName;
		this.referenceToChattingWindow=referenceToChattingWindow;
		this.serverIP=serverIP;
		new File(directoryToStoreRecievedFiles).mkdirs();		
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
		
		new Thread(new Runnable() {
			String message;
			@Override
			public void run() {
				while(true){
					//delete this try

					try{
						message=dataInputstream.readUTF();
					}catch(IOException e){
						JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
								+ "\n(1)If server is running"
								+ "\n(2)If you and server are on the same network "
								+ "\nPlease start the application again.","ERROR",JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
					//this condition will arrive only when the file has been chosen 
					//and a request to transfer file is sent to the server
					if(message.trim().equals("START_SENDING")){
						//send file in new thread so that the incoming messages are not blocked
						new Thread(new Runnable() {
							String nameOfFileToSend=referenceToChattingWindow.nameOfFileToSend;
							@Override
							public void run() {
								isSendingFile=true;
								JDialog dialog=null;
								JProgressBar progressBar=null;
								FileInputStream fileInputStream=null;
								BufferedInputStream bufferedInputStream=null;
								try{

									OutputStream outputStream=socket.getOutputStream();
									File fileToSend=new File(nameOfFileToSend);

									dialog=new JDialog(referenceToChattingWindow, "", JDialog.ModalityType.MODELESS);
									dialog.setUndecorated(true);
									progressBar=new JProgressBar(0,(int)fileToSend.length());
									progressBar.setStringPainted(true);
									progressBar.setString("Sending file to server...");
									dialog.add(BorderLayout.CENTER, progressBar);
									dialog.setSize(200, 20);
									dialog.setLocationRelativeTo(referenceToChattingWindow);
									dialog.setVisible(true);

									byte byteArrayOfFileToSend[]=new byte[99999999];
									fileInputStream=new FileInputStream(fileToSend);
									bufferedInputStream=new BufferedInputStream(fileInputStream);
									int bytesRead=bufferedInputStream.read(byteArrayOfFileToSend, 0, byteArrayOfFileToSend.length);
									int totalBytesRead=bytesRead;
									progressBar.setValue(totalBytesRead);
									outputStream.write(byteArrayOfFileToSend, 0,bytesRead);
									while((bytesRead=bufferedInputStream.read(byteArrayOfFileToSend, 0, byteArrayOfFileToSend.length))!=-1){
										totalBytesRead+=bytesRead;
										progressBar.setValue(totalBytesRead);
										outputStream.write(byteArrayOfFileToSend, 0, bytesRead);
									}

									outputStream.flush();

								}catch(Exception e){
									JOptionPane.showMessageDialog(referenceToChattingWindow,"Failed to send the file !!!","ERROR",JOptionPane.ERROR_MESSAGE);	
								}
								finally{
									try {
										bufferedInputStream.close();
										fileInputStream.close();
									} catch (IOException e) {

									}
								}
								dialog.dispose();
								isSendingFile=false;
							}
						}).start();
					}

					//no need for runnable here
					//intentionally blocking readUTF call

					else if(message.startsWith("INITIATE_FILE_TRANSFER_FROM_SERVER_TO_CLIENT")){
						isRecievingFile=true;
						sendMessage("START_SENDING");
						final String recievedFileName=message.trim().substring(message.trim().lastIndexOf('#')+1, message.trim().length());
						final int recievedFileLength=Integer.valueOf(message.trim().substring(message.trim().lastIndexOf('@')+1, message.trim().lastIndexOf('#')));
						final String absoluteFilePathOfRecievedFile=directoryToStoreRecievedFiles+"\\"+recievedFileName;

						int totalBytesRead=0;
						int bytesReadThisTime=0;

						JDialog dialog=null;
						JProgressBar progressBar=null;

						dialog=new JDialog(referenceToChattingWindow, "", JDialog.ModalityType.MODELESS);
						dialog.setUndecorated(true);
						progressBar=new JProgressBar(0,recievedFileLength);
						progressBar.setStringPainted(true);
						progressBar.setString("Downloading file from server...");
						dialog.add(BorderLayout.CENTER, progressBar);
						dialog.setSize(200, 20);
						dialog.setLocationRelativeTo(referenceToChattingWindow);
						dialog.setVisible(true);

						File temp=new File(absoluteFilePathOfRecievedFile);
						if(temp.exists())
							temp.delete();
						temp=null;


						FileOutputStream fileOutputStream=null;
						BufferedOutputStream bufferedOutputStream=null;
						try{
							InputStream inputStream=socket.getInputStream();
							fileOutputStream=new FileOutputStream(absoluteFilePathOfRecievedFile,true);
							bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
							byte recievedbytes[]=new byte[99999999];
							System.out.println(recievedFileLength);
							bytesReadThisTime=inputStream.read(recievedbytes,0,recievedbytes.length);
							bufferedOutputStream.write(recievedbytes, 0, bytesReadThisTime);

							totalBytesRead=bytesReadThisTime;
							progressBar.setValue(totalBytesRead);

							while(totalBytesRead<recievedFileLength){
								bytesReadThisTime=inputStream.read(recievedbytes,0, recievedbytes.length);
								bufferedOutputStream.write(recievedbytes, 0, bytesReadThisTime);
								totalBytesRead+=bytesReadThisTime;
								progressBar.setValue(totalBytesRead);

							}
						}catch(Exception e){
							JOptionPane.showMessageDialog(referenceToChattingWindow,"Failed to recieve the file !!!","ERROR",JOptionPane.ERROR_MESSAGE);

						}
						finally{
							try {
								bufferedOutputStream.flush();
								bufferedOutputStream.close();
								fileOutputStream.flush();
								fileOutputStream.close();
							} catch (IOException e) {
							}
						}
						dialog.dispose();
						isRecievingFile=false;
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

