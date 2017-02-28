import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImageFilter;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JLabel;
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


	public TCPClient(ChattingWindow reference,String serverIP,String userName){
		System.out.println("i am client");
		new File(directoryToStoreRecievedFiles).mkdirs();
		referenceToChattingWindow=reference;
		try {
			socket=new Socket(serverIP,2222);
		} catch (IOException e1) {

		}

		try {
			dataOutputStream=new DataOutputStream(socket.getOutputStream());
			dataInputstream=new DataInputStream(socket.getInputStream());
		} catch (IOException e1) {
		
		}
		
		
		//sending name of the client here
		try {
			dataOutputStream.writeUTF(userName);
		} catch (IOException e) {

		}

		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					String message="";
					try {
						Thread.sleep(1);
						message=dataInputstream.readUTF();
						System.out.println(message);
						
						if(message.trim().equals("START_SENDING")){
							
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
										System.out.println("file sent by client ");
									}catch(Exception e){
										System.out.println(e);
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
						//no need to send acknowledgement message because no readUTF comes after this and
						//server has already started putting files in outputStream
						
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
									System.out.println("client recieving file...");
								}


								System.out.println("file recieved by client");
							}catch(Exception e){
								System.out.println(e.toString());
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
					} catch (IOException | InterruptedException e) {
						System.out.println(e);
					}
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

