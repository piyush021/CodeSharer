import java.awt.BorderLayout;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class UDPClientToGetIP {
	
	DatagramSocket clientSocket=null;
	String serverIP;
	ChattingWindow referenceToChattingWindow;
	
	public UDPClientToGetIP(ChattingWindow referenceToChattingWindow){
		this.referenceToChattingWindow=referenceToChattingWindow;
		startUDPClient();
	}

	private void startUDPClient() {
		
		JDialog dialog=null;
		dialog=new JDialog(referenceToChattingWindow, "", JDialog.ModalityType.MODELESS);
		dialog.setUndecorated(true);
		dialog.add(BorderLayout.CENTER, new JLabel("Connecting to server...please wait..."));
		dialog.setSize(210, 20);
		dialog.setLocationRelativeTo(referenceToChattingWindow);
		dialog.setVisible(true);

		try{
			clientSocket=new DatagramSocket();
			//broadcast message
			//server with poet no. 1111 will recieve the packets
			DatagramPacket packetToSend=new DatagramPacket("IAMTHECLIENT".getBytes()
					, "IAMTHECLIENT".getBytes().length
					,InetAddress.getByName("255.255.255.255"),1111);
			clientSocket.send(packetToSend);
			//sending same message again because UDP is unreliable
			packetToSend=new DatagramPacket("IAMTHECLIENT".getBytes()
					, "IAMTHECLIENT".getBytes().length
					,InetAddress.getByName("255.255.255.255"),1111);
			clientSocket.send(packetToSend);
			
			
		    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		    while (interfaces.hasMoreElements()) {
		        NetworkInterface iface = interfaces.nextElement();
		        //if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
		        //    continue;
		        for(InterfaceAddress interfaceAddress:iface.getInterfaceAddresses()){
		        	InetAddress broadcast=interfaceAddress.getBroadcast();
		        	if(broadcast==null)continue;
		        	packetToSend=new DatagramPacket("IAMTHECLIENT".getBytes()
							, "IAMTHECLIENT".getBytes().length
							, broadcast,1111);
					clientSocket.send(packetToSend);
					//just in case first packet got lost
					packetToSend=new DatagramPacket("IAMTHECLIENT".getBytes()
							, "IAMTHECLIENT".getBytes().length
							, broadcast,1111);
					clientSocket.send(packetToSend);
		        }
		    }
			
			while(true){
				byte bufferToRecieve[]=new byte[9999];
				DatagramPacket recievedPacket=new DatagramPacket(bufferToRecieve,bufferToRecieve.length);
				clientSocket.receive(recievedPacket);
				byte recievedBytes[]=recievedPacket.getData();
				String recievedString=new String(recievedBytes,0,recievedBytes.length);
				recievedString=recievedString.trim();
				if(recievedString.equals("IAMTHESERVER")){
					serverIP=recievedPacket.getAddress().getHostAddress();
					dialog.dispose();
					break;
				}
			}

		}catch(Exception e){			
			JOptionPane.showMessageDialog(referenceToChattingWindow,"FATAL ERROR : Failed to connect to server, check :-"
					+ "\n(1)If server is running"
					+ "\n(2)If you and server are on the same network "
					+ "\nPlease start the application again."
					+ "\n"+e.toString(),"ERROR",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

	}
	
	public String getIPAddressOfServer(){
		return serverIP;
	}
	
}
