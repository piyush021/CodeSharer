import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class TCPClientForFileTransfer {

	private ChattingWindow referenceToChattingWindow;
	private Socket socket;
	private DataOutputStream dataOutputStream;
	private DataInputStream dataInputstream;
	private String directoryToStoreRecievedFiles;
	private String userName="";
	private String serverIP="";
	public boolean isRecievingFile=false;
	public boolean isSendingfile=false;
	
	
	public TCPClientForFileTransfer(ChattingWindow referenceToChattingWindow,String serverIP,String userName,String defaultDirectory) {
		this.directoryToStoreRecievedFiles=defaultDirectory;
		this.userName=userName;
		this.referenceToChattingWindow=referenceToChattingWindow;
		this.serverIP=serverIP;
		new File(directoryToStoreRecievedFiles).mkdirs();		
	}
	
	public void startTCPClientForFileTransfer(){
		try {
			socket=new Socket(serverIP,3333);
			dataOutputStream=new DataOutputStream(socket.getOutputStream());
			dataInputstream=new DataInputStream(socket.getInputStream());
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
					+ "\n(1)If server is running"
					+ "\n(2)If you and server are on the same network "
					+ "\nPlease start the application again."
					+ "\n"+e1.toString(),"ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		//sending name of the client as first message
		try {
			dataOutputStream.writeUTF(userName);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
					+ "\n(1)If server is running"
					+ "\n(2)If you and server are on the same network "
					+ "\nPlease start the application again."
					+"\n"+e.toString(),"ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		new Thread(new Runnable() {
			String message;
			@Override
			public void run() {
				while(true){
					
					try{
						message=dataInputstream.readUTF();
					}catch(IOException e){
						System.out.println("file client"+e);
						JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
								+ "\n(1)If server is running"
								+ "\n(2)If you and server are on the same network "
								+ "\nPlease start the application again."
								+ "\n"+e.toString(),"ERROR",JOptionPane.ERROR_MESSAGE);
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
								isSendingfile=true;
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

									byte byteArrayOfFileToSend[]=new byte[5242880];
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
									JOptionPane.showMessageDialog(referenceToChattingWindow, "Sent To Server..");
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
								isSendingfile=false;
								dialog.dispose();
							}
						}).start();
					}

					//no need for runnable here
					//intentionally blocking readUTF call
					else if(message.startsWith("START_RECIEVING")){
						isRecievingFile=true;
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
							byte recievedbytes[]=new byte[5242880];
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
							JOptionPane.showMessageDialog(referenceToChattingWindow, "Download Complete.");
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
						isRecievingFile=false;
						dialog.dispose();
					}
					else{
						referenceToChattingWindow.listModelForFileName.addElement(message);
					}
				}
			}
		}).start();

	}
	public void sendMessage(String message){
		try {
			//ask server to start recieving
			dataOutputStream.writeUTF(message);
		} catch (IOException e) {

		}
	}
}
