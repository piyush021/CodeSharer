import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.IconUIResource;
import javax.swing.text.DefaultCaret;

class ChattingWindow extends JFrame{
	
    String stringUserName;
    JTextArea textAreaIncomingMessages;
	JTextArea textAreaOutgoingMessages;
    JButton buttonSend;
    JButton buttonAttachment;
    JScrollPane scrollingContainerTextAreaIncomingMessages;
    JScrollPane scrollingContainerTextAreaOutgoingMessages;
    UDPClientToGetIP udpClient;
    TCPClient tcpClient;
    String nameOfFileToSend="";
    
    public ChattingWindow(String stringUserName){
    	super();
    	////////////////////////////////////////////////////////////////////////////////////////////////////////
    	udpClient=new UDPClientToGetIP();
    	
		this.stringUserName=stringUserName;
		setTitle("CodeSharer");
		this.setIconImage(new ImageIcon(this.getClass().getResource("/mainIcon.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		//this.getContentPane().setBackground(Color.BLACK);
		textAreaIncomingMessages=new JTextArea();
		//textAreaIncomingMessages.setBackground(Color.LIGHT_GRAY);
		textAreaIncomingMessages.setLineWrap(true);
		textAreaIncomingMessages.setFont(new Font("",Font.BOLD,12));
		textAreaIncomingMessages.setMargin(new Insets(5,5,5,5));
		

		DefaultCaret caret=(DefaultCaret)textAreaIncomingMessages.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		
		textAreaOutgoingMessages=new JTextArea();
		//textAreaOutgoingMessages.setBackground(Color.LIGHT_GRAY);
		textAreaOutgoingMessages.setMargin(new Insets(5,5,5,5));
		textAreaOutgoingMessages.setLineWrap(true);
		buttonSend=new JButton("SEND");
		buttonSend.setBounds(420,600,70,30);
		buttonAttachment=new JButton();
		buttonAttachment.setBounds(420,550,70,50);
		Image scaledToFitAttachmentImage=null;
		try {
			scaledToFitAttachmentImage = ImageIO.read(this.getClass().getResource("/attachmentIcon.png")).getScaledInstance(50, 50,Image.SCALE_SMOOTH);
		} catch (IOException e1) {
		
		}
		buttonAttachment.setIcon(new ImageIcon(scaledToFitAttachmentImage));
		buttonAttachment.setOpaque(false);
		buttonAttachment.setContentAreaFilled(false);
		buttonAttachment.setBorderPainted(false);
		
		scrollingContainerTextAreaIncomingMessages=new JScrollPane(textAreaIncomingMessages);
		scrollingContainerTextAreaOutgoingMessages=new JScrollPane(textAreaOutgoingMessages);
		scrollingContainerTextAreaIncomingMessages.setBounds(10,10,480,530);
		scrollingContainerTextAreaOutgoingMessages.setBounds(10,550,400,80);
		scrollingContainerTextAreaIncomingMessages.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		setLayout(null);
		add(scrollingContainerTextAreaIncomingMessages);
		add(scrollingContainerTextAreaOutgoingMessages);
		add(buttonSend);
		add(buttonAttachment);
		
		textAreaIncomingMessages.setEditable(false);
		setSize(506,670);
		//Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    //int x = (int) ((dimension.getWidth() - 506) / 2);
	    //int y = 20;
	    //setLocation(x, y);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
		addWindowListener(new ExitApplication());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		tcpClient=new TCPClient(this, udpClient.serverIP,stringUserName);
		
		buttonSend.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				if(tcpClient.isSendingFile||tcpClient.isRecievingFile){
					JOptionPane.showMessageDialog(getRootPane(),"You can not send messagges while you upload or Download files !!!","ERROR",JOptionPane.ERROR_MESSAGE);
				}else{
					String stringMessage=ChattingWindow.this.stringUserName+" says:"
							+"\n"+textAreaOutgoingMessages.getText();
					textAreaOutgoingMessages.setText("");
					tcpClient.sendMessage(stringMessage);
				}
			}
		});
		
		
		buttonAttachment.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser fileChooser=new JFileChooser();
				fileChooser.showDialog(rootPane,"Send");
				File fileToSend=fileChooser.getSelectedFile();
				if(((int)fileToSend.length())>5242880){
					JOptionPane.showMessageDialog(getRootPane(),"Attachment of more than 5 mb is not allowed !!!","ERROR",JOptionPane.ERROR_MESSAGE);
				}else{
					try{
						nameOfFileToSend=fileToSend.getAbsolutePath();
						tcpClient.sendMessage("INITIATE_FILE_TRANSFER_FROM_CLIENT_TO_SERVER@"+(int)fileToSend.length()+"#"+fileToSend.getName());
					}catch(Exception exception){

					}
				}
			}
		});
    }
    public static void main(String args[]){
    	new ChattingWindow("saurabh");
    }
   
}
