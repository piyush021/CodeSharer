import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
class LoginWindow extends JFrame{

	JLabel labelUserName;
	JLabel labelPassword;
	JButton buttonSubmit;
	JTextField textFieldUserName;
	JPasswordField passwordFieldPassword;
	
		@SuppressWarnings("deprecation")
		public LoginWindow(){
			super();
			this.setTitle("CodeSharer");
			this.setIconImage(new ImageIcon(this.getClass().getResource("/mainIcon.png")).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		    labelUserName = new JLabel("Enter Username");
		    textFieldUserName = new JTextField(20);
		    labelPassword = new JLabel("Enter Password");
		    passwordFieldPassword = new JPasswordField(20);
		    buttonSubmit = new JButton("LogIn");
		    setLayout(new FlowLayout());
		    add(labelUserName);
		    add(textFieldUserName);
		    add(labelPassword);
		    add(passwordFieldPassword);
		    add(buttonSubmit);
		    setSize(300,175);
		    setResizable(false);
		    setLocationRelativeTo(null);
		    setVisible(true);   
		    addWindowListener(new ExitApplication());
		    
		    buttonSubmit.addActionListener(new ActionListener(){
		    	@Override
		    	public void actionPerformed(ActionEvent e){
				    final String stringUsername = textFieldUserName.getText();
				    if(stringUsername.equals("")){
				    	JOptionPane.showMessageDialog(getRootPane(),"Invalid UserName !!!","ERROR",JOptionPane.ERROR_MESSAGE);
				    	return;
				    }
				    String stringPassword = passwordFieldPassword.getText();
				    if(stringPassword.equals("")){
					    new Thread(new Runnable(){
					    	@Override
					        public void run(){
					    		JFileChooser fileChooser=new JFileChooser();
							    fileChooser.setDialogTitle("Select Default Directory");
							    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							    fileChooser.setAcceptAllFileFilterUsed(false);
							    String defaultDirectory="";
							    if(fileChooser.showDialog(LoginWindow.this,"Select")==JFileChooser.APPROVE_OPTION){
							  		defaultDirectory=fileChooser.getSelectedFile().getAbsolutePath();
							   	}else{
							   		System.exit(0);
							    }
					    		new ChattingWindow(stringUsername,defaultDirectory);
					    	}	        
					    }).start();
					    LoginWindow.this.dispose();
				    }else{
				    	JOptionPane.showMessageDialog(getRootPane(),"Incorrect password !!!","ERROR",JOptionPane.ERROR_MESSAGE);
				    }
		    	}
			});	   
	}
	    
	public static void main(String args[]){
	    new LoginWindow();
	}
}






