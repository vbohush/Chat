package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class Client extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	DefaultStyledDocument doc = new DefaultStyledDocument();	
	private JTextPane jtpChat = new JTextPane(doc);
	
	private JTextField jtfMessage = new JTextFieldLimit(1024);
	private JLabel jlblToUser = new JLabel("");
	private UserList jlUsers = new UserList(this);
	
	private JCheckBox jcbBold = new JCheckBox("B");
	private JCheckBox jcbItalic = new JCheckBox("I");
	private ColorPanel colorPanel = new ColorPanel(Color.BLACK);
	private int fontStyle = Font.PLAIN;
	
	private PrintWriter toServer;
	private Scanner fromServer;

	public Client(PrintWriter toServer, Scanner fromServer) {
		this(toServer, fromServer, "n", "y", Color.BLACK.getRGB() + "");
	}
	
	public Client(PrintWriter toServer, Scanner fromServer, String isFontBold, String isFontItalic, String fontColor) {
		this.toServer = toServer;
		this.fromServer = fromServer;
		setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		
		JPanel jpChat = new JPanel(new BorderLayout(5, 5));
		jtpChat.setEditable(false);
		jtpChat.setBackground(Color.WHITE);
		JScrollPane jsp = new JScrollPane(jtpChat);
	    jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        

		jpChat.add(jsp, BorderLayout.CENTER);
		jpChat.setBorder(new TitledBorder(new EmptyBorder(1, 1, 1, 1), "Chat"));
		mainPanel.add(jpChat, BorderLayout.CENTER);
	    
		
	    JPanel jpMessage = new JPanel(new BorderLayout(5, 5));
	    jpMessage.setBorder(new EmptyBorder(0, 5, 0, 5));
	    jpMessage.add(jtfMessage, BorderLayout.CENTER);
	    
	    jlblToUser.setVisible(false);
	    jpMessage.add(jlblToUser, BorderLayout.WEST);
	    jlblToUser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				jlblToUser.setText("");
				jlblToUser.setVisible(false);
			}
		});

	    JPanel jpMessageOptions = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
	    jcbBold.setFont(jcbBold.getFont().deriveFont(Font.BOLD));
	    jcbItalic.setFont(jcbItalic.getFont().deriveFont(Font.ITALIC));
	    jpMessageOptions.add(jcbBold);	    
	    jpMessageOptions.add(jcbItalic);
	    jpMessageOptions.add(colorPanel);
	    JButton jbtnSend = new JButton("Send");
	    jpMessageOptions.add(jbtnSend);
	    
	    
	    jpMessage.add(jpMessageOptions, BorderLayout.EAST);
	    
	    mainPanel.add(jpMessage, BorderLayout.SOUTH);
		JPanel jpUsers = new JPanel(new BorderLayout(5, 5));

		JScrollPane jspUsers = new JScrollPane(jlUsers);
		jspUsers.setPreferredSize(new Dimension(150, 150));
		jpUsers.add(jspUsers, BorderLayout.CENTER);
		jpUsers.setBorder(new TitledBorder(new EmptyBorder(1, 1, 1, 1), "Users"));
		mainPanel.add(jpUsers, BorderLayout.EAST);
		  
		
		
	    add(mainPanel, BorderLayout.CENTER);
	    
		new ReceiveMessage();
		jtfMessage.requestFocus();
		
		//change font style
		ActionListener changeFontStyle = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				fontStyle = Font.PLAIN;
				if(jcbBold.isSelected()){
					fontStyle = Font.BOLD;
				}
				if(jcbItalic.isSelected()){
					if(fontStyle == Font.BOLD) {
						fontStyle = Font.BOLD + Font.ITALIC;
					} else {
						fontStyle = Font.ITALIC;
					}
				}
				jtfMessage.setFont(jtfMessage.getFont().deriveFont(fontStyle));
				jtfMessage.requestFocus();
			}
		};
		jcbBold.addActionListener(changeFontStyle);
		jcbItalic.addActionListener(changeFontStyle);
		colorPanel.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseReleased(MouseEvent e) {
				jtfMessage.setForeground(colorPanel.getColor());	
			}
		});
		
		//send message
		ActionListener sendMessage = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!jtfMessage.getText().equals("")) {
					if(jlblToUser.isVisible()) {
						Client.this.toServer.println("2");
						Client.this.toServer.println(jlblToUser.getText().substring(2));
						Client.this.toServer.println(fontStyle + "");
						Client.this.toServer.println(colorPanel.getColor().getRGB());
						Client.this.toServer.println(jtfMessage.getText());
						Client.this.toServer.flush();
						jtfMessage.setText("");
					} else {
						Client.this.toServer.println("1");
						Client.this.toServer.println(fontStyle + "");
						Client.this.toServer.println(colorPanel.getColor().getRGB());
						Client.this.toServer.println(jtfMessage.getText());
						Client.this.toServer.flush();
						jtfMessage.setText("");
					}
				}
				jtfMessage.requestFocus();
			}
		};
	    
		jtfMessage.addActionListener(sendMessage);
		jbtnSend.addActionListener(sendMessage);
		
		//apply font settings
		if(isFontBold.equals("y")) {
			jcbBold.setSelected(true);
			
		} else {
			jcbBold.setSelected(false);
		}
		if(isFontItalic.equals("y")) {
			jcbItalic.setSelected(true);
			
		} else {
			jcbItalic.setSelected(false);
		}
		changeFontStyle.actionPerformed(null);
		
		try {
			Color newFontColor = new Color(Integer.parseInt(fontColor));
			colorPanel.setColor(newFontColor);
			jtfMessage.setForeground(colorPanel.getColor());
		} catch (NumberFormatException e) {
		}

	}
	
	public String getSettings() {
		String result = "";
		if(jcbBold.isSelected()) {
			result += "isfontbold=y\r\n";
		} else {
			result += "isfontbold=n\r\n";
		}
		if(jcbItalic.isSelected()) {
			result += "isfontitalic=y\r\n";
		} else {
			result += "isfontitalic=n\r\n";
		}
		result += "fontcolor=" + colorPanel.getColor().getRGB() + "\r\n";
		return result;
	}

	public void setFocus() {
		jtfMessage.requestFocus();
	}
	
	public void setPmUser(String userName) {
		if(jlblToUser.getText().equals("> " + userName)) {
			jlblToUser.setText("");
			jlblToUser.setVisible(false);
		} else {
			jlblToUser.setText("> " + userName);
			jlblToUser.setVisible(true);			
		}
		jtfMessage.requestFocus();
	}
	
	//receive messages and users list
	class ReceiveMessage implements Runnable {
		public ReceiveMessage() {
			Thread thread = new Thread(this);
			thread.start();
		}
		public void run() {
			try {
				while(true) {
					String command = fromServer.nextLine();
					if((command.equals("1")) || (command.equals("3"))) { //new message
						
						Calendar messageTime = new GregorianCalendar();
						String time = String.format("[%02d:%02d:%02d]", messageTime.get(Calendar.HOUR_OF_DAY), messageTime.get(Calendar.MINUTE), messageTime.get(Calendar.SECOND));
						
						int messageFontStyle = Integer.parseInt(fromServer.nextLine());
						Color messageColor = new Color(Integer.parseInt(fromServer.nextLine()));
						String message = fromServer.nextLine();
						
						try {
							Color privateMessageColor = new Color(217, 217, 217);
							
							SimpleAttributeSet aset = new SimpleAttributeSet();
							if(command.equals("3")) {
								StyleConstants.setBackground(aset, privateMessageColor);
							}							
							StyleConstants.setForeground(aset, Color.GRAY);
							StyleConstants.setBold(aset, true);	
							StyleConstants.setItalic(aset, false);
							doc.insertString(doc.getLength(), time, aset);
							
							aset = new SimpleAttributeSet();
							if(command.equals("3")) {
								StyleConstants.setBackground(aset, privateMessageColor);
							}							
							StyleConstants.setForeground(aset, messageColor);
							if(messageFontStyle == Font.PLAIN) {
								StyleConstants.setBold(aset, false);	
								StyleConstants.setItalic(aset, false);							
							} else if(messageFontStyle == Font.BOLD) {
								StyleConstants.setBold(aset, true);	
								StyleConstants.setItalic(aset, false);
							} else if(messageFontStyle == Font.ITALIC) {
								StyleConstants.setBold(aset, false);	
								StyleConstants.setItalic(aset, true);		
							} else if(messageFontStyle == Font.BOLD + Font.ITALIC) {
								StyleConstants.setBold(aset, true);	
								StyleConstants.setItalic(aset, true);		
							}
							doc.insertString(doc.getLength(), message + " \n", aset);
							jtpChat.setCaretPosition(jtpChat.getDocument().getLength());

						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					
					} else if(command.equals("2")) { //list of clients
						int usersCount = Integer.parseInt(fromServer.nextLine());
						String[] users = new String[usersCount];
						for (int i = 0; i < users.length; i++) {
							users[i] = fromServer.nextLine();
						}
						jlUsers.setData(users);						
					}
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}
		}
	}

}
