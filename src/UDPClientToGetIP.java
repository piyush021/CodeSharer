import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class UDPClientToGetIP {
	
	DatagramSocket clientSocket=null;
	String serverIP;
	
	public UDPClientToGetIP(){
		createClient();
	}

	private void createClient() {
		try{
			clientSocket=new DatagramSocket();
			DatagramPacket packetToSend=new DatagramPacket("IAMTHECLIENT".getBytes()
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
					break;
				}
			}

		}catch(Exception e){			
		
		}

	}
	
}
