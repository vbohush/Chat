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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	
	private DefaultStyledDocument doc = new DefaultStyledDocument();	
	private JTextPane jtpChat = new JTextPane(doc);
	
	private JTextField jtfMessage = new JTextFieldLimit(1024);
	private JLabel jlblToUser = new JLabel("");
	private UserList jlUsers;
	
	private JCheckBox jcbBold = new JCheckBox("B");
	private JCheckBox jcbItalic = new JCheckBox("I");
	private ColorPanel colorPanel = new ColorPanel(Color.BLACK);
	private int fontStyle = Font.PLAIN;
	
	private PrintWriter toServer;
	private Scanner fromServer;
	private DatagramSocket serverSocket;
	
	private String checkDuplicates;
	private boolean isNonServerMode;
	private int port;
	private String userName;
	
	public Client(boolean isNonServerMode, PrintWriter toServer, Scanner fromServer, DatagramSocket serverSocket, int port, String userName, boolean isFontBold, boolean isFontItalic, String fontColor, boolean isAdmin) {
		this.isNonServerMode = isNonServerMode;
		this.port = port;
		this.userName = userName;
		if(!isNonServerMode) {
			jlUsers = new UserList(this, isAdmin);
			this.toServer = toServer;
			this.fromServer = fromServer;
		} else {
			this.serverSocket = serverSocket;
			try {
				serverSocket.setBroadcast(true);
			} catch (SocketException e1) {
			}
		}
		
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
	    
	    if(!isNonServerMode) {
	    	JPanel jpUsers = new JPanel(new BorderLayout(5, 5));

			JScrollPane jspUsers = new JScrollPane(jlUsers);
			jspUsers.setPreferredSize(new Dimension(170, 170));
			jpUsers.add(jspUsers, BorderLayout.CENTER);
			jpUsers.setBorder(new TitledBorder(new EmptyBorder(1, 1, 1, 1), "Users"));
			mainPanel.add(jpUsers, BorderLayout.EAST);
	    }
		
		
	    add(mainPanel, BorderLayout.CENTER);
	    if(isNonServerMode) {
	    	new ReceiveMessageUDP();
	    } else {
	    	new ReceiveMessageTCP();
	    }
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
					if (Client.this.isNonServerMode) {// UDP-mmode

						try {
							InetAddress IPAddress = InetAddress.getByName("255.255.255.255");
							checkDuplicates = System.currentTimeMillis() + "" + Math.random();
							String sentence = checkDuplicates + "\n" + fontStyle + "\n" + colorPanel.getColor().getRGB() + "\n " + Client.this.userName + ": " + jtfMessage.getText();
							byte[] sendData = sentence.getBytes(StandardCharsets.UTF_8.name());
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Client.this.port);
							Client.this.serverSocket.send(sendPacket);
							showNewMessage(fontStyle, colorPanel.getColor(), " " + Client.this.userName + ": " + jtfMessage.getText(), false);
							jtfMessage.setText("");
						} catch (UnknownHostException e1) {
						} catch (IOException e1) {
						}

					} else {// TCP-mode
						if (jlblToUser.isVisible()) {// private message
							Client.this.toServer.println("2");
							Client.this.toServer.println(jlblToUser.getText().substring(2));
							Client.this.toServer.println(fontStyle);
							Client.this.toServer.println(colorPanel.getColor().getRGB());
							Client.this.toServer.println(jtfMessage.getText());
							Client.this.toServer.flush();
							jtfMessage.setText("");
						} else {// public message
							Client.this.toServer.println("1");
							Client.this.toServer.println(fontStyle);
							Client.this.toServer.println(colorPanel.getColor().getRGB());
							Client.this.toServer.println(jtfMessage.getText());
							Client.this.toServer.flush();
							jtfMessage.setText("");
						}

					}
				}
				jtfMessage.requestFocus();
			}
		};
	    
		jtfMessage.addActionListener(sendMessage);
		jbtnSend.addActionListener(sendMessage);
		
		//apply font settings
		jcbBold.setSelected(isFontBold);
		jcbItalic.setSelected(isFontItalic);
		changeFontStyle.actionPerformed(null);
		
		try {
			Color newFontColor = new Color(Integer.parseInt(fontColor));
			colorPanel.setColor(newFontColor);
			jtfMessage.setForeground(colorPanel.getColor());
		} catch (NumberFormatException e) {
		}

	}
	
	public String getSettings() {
		String result = "isfontbold=" + jcbBold.isSelected() + "\r\n";
		result += "isfontitalic=" + jcbItalic .isSelected() + "\r\n";
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
	
	
	
	public void banUser(String ip) {
		Client.this.toServer.println("3");
		Client.this.toServer.println(ip);
		Client.this.toServer.flush();
		jtfMessage.requestFocus();
	}
	
	private synchronized void showNewMessage(int messageFontStyle, Color messageColor, String message, boolean pm) {
		Calendar messageTime = new GregorianCalendar();
		String time = String.format("[%02d:%02d:%02d]", messageTime.get(Calendar.HOUR_OF_DAY), messageTime.get(Calendar.MINUTE), messageTime.get(Calendar.SECOND));
		try {
			Color privateMessageColor = new Color(217, 217, 217);
			SimpleAttributeSet aset = new SimpleAttributeSet();
			if(pm) {
				StyleConstants.setBackground(aset, privateMessageColor);
			}							

			StyleConstants.setForeground(aset, Color.GRAY);
			StyleConstants.setBold(aset, true);	
			StyleConstants.setItalic(aset, false);
			doc.insertString(doc.getLength(), time, aset);
			
			aset = new SimpleAttributeSet();
			if(pm) {
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
			//limit lines count in chat
			String text = doc.getText(0, doc.getLength()); 
			if(text.split("\n").length > 200) {
				doc.remove(0, text.indexOf("\n") + 1);
			}
			
			jtpChat.setCaretPosition(doc.getLength());
			
		} catch (BadLocationException e2) {}
	}
	
	// receive messages and users list in UDP-mode
	class ReceiveMessageUDP implements Runnable {
		public ReceiveMessageUDP() {
			Thread thread = new Thread(this);
			thread.start();
		}

		@Override
		public void run() {
			while(true) {
			    byte[] buf = new byte[4200];  
			      
			    DatagramPacket dp = new DatagramPacket(buf, buf.length);  
			    try {
					Client.this.serverSocket.receive(dp);
					
					String[] messageData;
					try {
						messageData = new String(dp.getData(), 0, dp.getLength(), StandardCharsets.UTF_8.name()).split("\n");
					} catch (UnsupportedEncodingException e) {
						messageData = new String(dp.getData(), 0, dp.getLength()).split("\n");
					}
					
					if(!checkDuplicates.equals(messageData[0])) {
						int messageFontStyle = Integer.parseInt(messageData[1]);
						Color messageColor = new Color(Integer.parseInt(messageData[2]));
						String message = messageData[3];
						showNewMessage(messageFontStyle, messageColor, message, false);
					}
				} catch (IOException e1) {
				}			    
			}
		}

	}
	
	//receive messages and users list in TCP-mode
	class ReceiveMessageTCP implements Runnable {
		public ReceiveMessageTCP() {
			Thread thread = new Thread(this);
			thread.start();
		}
		public void run() {
			try {
				while(true) {
					String command = fromServer.nextLine();
					if((command.equals("1")) || (command.equals("3"))) { //new message
						
						int messageFontStyle = Integer.parseInt(fromServer.nextLine());
						Color messageColor = new Color(Integer.parseInt(fromServer.nextLine()));
						String message = fromServer.nextLine();
						
						showNewMessage(messageFontStyle, messageColor, message, command.equals("3") ? true: false);
					
					} else if(command.equals("2")) { //list of clients
						int usersCount = Integer.parseInt(fromServer.nextLine());
						String[] users = new String[usersCount];
						String[] ips = new String[usersCount];
						
						for (int i = 0; i < users.length; i++) {
							users[i] = fromServer.nextLine();
							ips[i] = fromServer.nextLine();
							
						}
						//check if private message user is online
						if(jlblToUser.isVisible()) {
							boolean isUserOnline = false;
							for (int i = 0; i < users.length; i++) {
								if(("> " + users[i]).equals(jlblToUser.getText())) {
									isUserOnline = true;
									break;
								};
							}
							if(!isUserOnline) {
								jlblToUser.setText("");
								jlblToUser.setVisible(false);
							}
						}
						
						jlUsers.setData(userName, users, ips);						
					} else if(command.equals("4")) { //disconnect
						fromServer.close();
						toServer.close();
						JOptionPane.showMessageDialog(null, "You are banned", "Warning", JOptionPane.WARNING_MESSAGE);
						System.exit(1);
					}
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}
		}
	}

}
