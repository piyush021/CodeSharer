import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    JScrollPane scrollingContainerForListOfFilesPresentInServer;
    JList<String> listOfFilesPresentInServer;
    DefaultListModel<String> listModelForFileName;
    JScrollPane scrollingContainerForListOfClientsOnline;
    JList<String> listOfClientsOnline;
    DefaultListModel<String> listModelForClientName;
    TCPClient tcpClient;
    String nameOfFileToSend="";
    String defaultDirectory;
    TCPClientForFileTransfer tcpClientForFileTransfer;
    
    public ChattingWindow(String stringUserName, String defaultDirectory){
    	super();
    	this.stringUserName=stringUserName;
    	this.defaultDirectory=defaultDirectory;
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
			buttonAttachment.setIcon(new ImageIcon(scaledToFitAttachmentImage));
			buttonAttachment.setOpaque(false);
			buttonAttachment.setContentAreaFilled(false);
			buttonAttachment.setBorderPainted(false);
		} catch (IOException e1) {
			//failed to load the image as button icon
			buttonAttachment=new JButton("Attach");
		}
		
		scrollingContainerTextAreaIncomingMessages=new JScrollPane(textAreaIncomingMessages);
		scrollingContainerTextAreaOutgoingMessages=new JScrollPane(textAreaOutgoingMessages);
		scrollingContainerTextAreaIncomingMessages.setBounds(10,10,480,530);
		scrollingContainerTextAreaOutgoingMessages.setBounds(10,550,400,80);
		scrollingContainerTextAreaIncomingMessages.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		
		//setting the list of files present in server here
		listModelForFileName=new DefaultListModel<>();
		listModelForFileName.addElement("     FILES ON SERVER");
		listModelForFileName.addElement("");
		listOfFilesPresentInServer=new JList<>(listModelForFileName);
		listOfFilesPresentInServer.setSelectedIndex(0);
		listOfFilesPresentInServer.setFont(new Font("Arial",Font.BOLD,10));
		listOfFilesPresentInServer.setBorder(new EmptyBorder(5,5,5,5));
		listOfFilesPresentInServer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollingContainerForListOfFilesPresentInServer=new JScrollPane(listOfFilesPresentInServer);
		scrollingContainerForListOfFilesPresentInServer.setBounds(500,10,136,310);
		scrollingContainerForListOfFilesPresentInServer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollingContainerForListOfFilesPresentInServer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//setting the list of clients online here
		listModelForClientName=new DefaultListModel<>();
		listModelForClientName.addElement("        ACTIVE USERS");
		listModelForClientName.addElement("");
		listOfClientsOnline=new JList<>(listModelForClientName);
		listOfClientsOnline.setFont(new Font("Arial",Font.BOLD,10));
		listOfClientsOnline.setBorder(new EmptyBorder(5,5,5,5));
		listOfClientsOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOfClientsOnline.setSelectedIndex(0);
		//making unselectable list of online clients
		listOfClientsOnline.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					listOfClientsOnline.setSelectedIndex(0);
				}
			}
		});
		scrollingContainerForListOfClientsOnline=new JScrollPane(listOfClientsOnline);
		scrollingContainerForListOfClientsOnline.setBounds(500,330,136,300);
		scrollingContainerForListOfClientsOnline.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollingContainerForListOfClientsOnline.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		setLayout(null);
		add(scrollingContainerTextAreaIncomingMessages);
		add(scrollingContainerTextAreaOutgoingMessages);
		add(scrollingContainerForListOfFilesPresentInServer);
		add(scrollingContainerForListOfClientsOnline);
		add(buttonSend);
		add(buttonAttachment);
		
		textAreaIncomingMessages.setEditable(false);
		//setSize(506,670);
		setSize(650,670);
		setLocationRelativeTo(null);
	
		setResizable(false);
		addWindowListener(new ExitApplication());
		setVisible(true);
		startClients();
	}
    
    private void startClients(){
    	//constructor of UDPClient will return only when IP of server is discovered
    	UDPClientToGetIP udpClient=new UDPClientToGetIP(ChattingWindow.this);
    	tcpClient=new TCPClient(this, udpClient.getIPAddressOfServer(), stringUserName);
    	tcpClient.startTCPClient();
    	tcpClientForFileTransfer=new TCPClientForFileTransfer(this ,udpClient.getIPAddressOfServer(),stringUserName,defaultDirectory);
    	tcpClientForFileTransfer.startTCPClientForFileTransfer();
        	
    	buttonSend.addActionListener(new ActionListener(){
    		@Override
    		public void actionPerformed(ActionEvent e){
    			if(textAreaOutgoingMessages.getText().equals(""))
    				return;
    			String stringMessage=ChattingWindow.this.stringUserName+" says:"
    					+"\n\n"+textAreaOutgoingMessages.getText();
    			textAreaOutgoingMessages.setText("");
    			tcpClient.sendMessage(stringMessage);

    		}
    	});


    	buttonAttachment.addActionListener(new ActionListener(){
    		@Override
    		public void actionPerformed(ActionEvent e){
    			if(tcpClientForFileTransfer.isSendingfile)
    				return;
    			JFileChooser fileChooser=new JFileChooser();
    			fileChooser.setDialogTitle("Select File To Send");
    			if(fileChooser.showDialog(rootPane,"Send")!=JFileChooser.APPROVE_OPTION)
    				return;
    			File fileToSend=fileChooser.getSelectedFile();
    			if(fileToSend==null)
    				return;
    			if(((int)fileToSend.length())>5242880){
    				
    				JOptionPane.showMessageDialog(getRootPane(),"Attachment of more than 5 mb is not allowed !!!","ERROR",JOptionPane.ERROR_MESSAGE);
    			
    			}else{

    				nameOfFileToSend=fileToSend.getAbsolutePath();
    				//sending command to server to start file transfer
    				tcpClientForFileTransfer.sendMessage("INITIATE_FILE_TRANSFER_FROM_CLIENT_TO_SERVER@"+(int)fileToSend.length()+"#"+fileToSend.getName());
    				
    			}
    		}
    	});
    	
    	//adding on click listener to jlist of files on server here
    	listOfFilesPresentInServer.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					if(tcpClientForFileTransfer.isRecievingFile)
						return;
					String fileNameToDownload=listOfFilesPresentInServer.getSelectedValue();
					if(fileNameToDownload.equals("")){
						listOfFilesPresentInServer.setSelectedIndex(0);
						return;
					}
					if(listOfFilesPresentInServer.getSelectedIndex()!=0){
						int dialogResult = JOptionPane.showConfirmDialog (null, "Would You Like To Download File "+listOfFilesPresentInServer.getSelectedValue()+" ?","Download File",JOptionPane.YES_NO_OPTION);
						if(dialogResult == JOptionPane.YES_OPTION){
							tcpClientForFileTransfer.sendMessage("INITIATE_FILE_TRANSFER_FROM_SERVER_TO_CLIENT@#"+listOfFilesPresentInServer.getSelectedValue());
						}
						listOfFilesPresentInServer.setSelectedIndex(0);
					}
				}
			}
		});

    }
    
}
