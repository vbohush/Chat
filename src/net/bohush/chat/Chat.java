package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Chat extends JPanel{
	private static final long serialVersionUID = 1L;
	private JTextField jtfServerPort;
	private JTextField jtfServerCount;
	private JTextField jtfConnectToIp;
	private JTextField jtfConnectToPort;
	private JTextField jtfUserName;
	
	private JFrame frame;
	
	private JPanel jpStart = new JPanel(new GridLayout(1, 2, 5, 5));
	private File serverConfigFile = new File(this.getClass().getResource("/").getPath() + "Server.txt");
	private File clientConfigFile = new File(this.getClass().getResource("/").getPath() + "Client.txt");
	private String charsetName = StandardCharsets.UTF_8.name();
	
	public Chat(JFrame frame)  {
		this.frame = frame;
		//server config
		String serverPort = "2014";
		String maxUsersCount = "5";
		if(serverConfigFile.exists()) {
			Scanner serverInput;
			try {
				serverInput = new Scanner(serverConfigFile, charsetName);
				HashMap<String, String> configs = new HashMap<>();
				while(serverInput.hasNextLine()) {
					String nextLine = serverInput.nextLine();
					String[] params = nextLine.split("=");
					if(params.length > 1) {
						configs.put(params[0].toLowerCase(), params[1]);	
					}				
				}
				serverInput.close();
				if(configs.get("port") != null) {
					serverPort = configs.get("port");					
				}
				if(configs.get("maxuserscount") != null) {
					maxUsersCount = configs.get("maxuserscount");
				}
			} catch (IOException e) {
			}
		}
		
		//client config
		String connectToIP = "127.0.0.1";
		String connectToPort = "2014";
		String userName = "User";
		if(clientConfigFile.exists()) {
			Scanner clientInput;
			try {
				clientInput = new Scanner(clientConfigFile, charsetName);
				HashMap<String, String> configs = new HashMap<>();
				while(clientInput.hasNextLine()) {
					String nextLine = clientInput.nextLine();
					String[] params = nextLine.split("=");
					if(params.length > 1) {
						configs.put(params[0].toLowerCase(), params[1]);	
					}				
				}
				clientInput.close();
				if(configs.get("ip") != null) {
					connectToIP = configs.get("ip");
				}
				if(configs.get("port") != null) {
					connectToPort = configs.get("port");					
				}
				if(configs.get("username") != null) {
					userName = configs.get("username");			
				}
			} catch (IOException e) {
			}
		}
		
		//build UI
		setLayout(new BorderLayout());
		jpStart.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//Server UI
		JPanel jpServer = new JPanel(new BorderLayout());
		jpServer.setBorder(new TitledBorder("Server"));
		
		JPanel jpConfigServer = new JPanel(new GridLayout(3, 3, 5, 20));
		jpConfigServer.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JLabel jlblServerPort = new JLabel("Port Number: ");
		jlblServerPort.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigServer.add(jlblServerPort);
		jtfServerPort = new JTextField(serverPort, 7);
		jpConfigServer.add(jtfServerPort);
		
		JLabel jlblServerCount = new JLabel("Max Users Count: ");
		jlblServerCount.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigServer.add(jlblServerCount);
		jtfServerCount = new JTextField(maxUsersCount, 7);
		jpConfigServer.add(jtfServerCount);
		
		jpConfigServer.add(new JLabel(" "));
		jpConfigServer.add(new JLabel(" "));
		
		JPanel jpStartServer = new JPanel(new BorderLayout());
		jpStartServer.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton jbtnStartServer = new JButton("Start Server");
		jpStartServer.add(jbtnStartServer, BorderLayout.CENTER);		
		
		jpServer.add(jpConfigServer, BorderLayout.CENTER);
		jpServer.add(jpStartServer, BorderLayout.SOUTH);
		
		jpStart.add(jpServer);
		
		//start server
		jbtnStartServer.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
			
				int port = 0;
				try {
					port = Integer.parseInt(jtfServerPort.getText());
					if((port < 1) || (port > 65535)) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(null, "\"Port Number\" must be an integer between 1 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
					jtfServerPort.requestFocus();
					return;
				}
				int maxUsersCount = 0;
				try {
					maxUsersCount = Integer.parseInt(jtfServerCount.getText());
					if(maxUsersCount < 2) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(null, "\"Max Users Count\" must be an integer between 2 and " + Integer.MAX_VALUE, "Error", JOptionPane.ERROR_MESSAGE);
					jtfServerCount.requestFocus();
					return;
				}

				try {
					ServerSocket serverSocket = new ServerSocket(port);
					
					try {					
						PrintWriter output = new PrintWriter(serverConfigFile, charsetName);
						output.write("port=" + port + "\r\nmaxuserscount=" + maxUsersCount);
						output.close();
					} catch (IOException e2) {
					}
					
					Chat.this.frame.setSize(640, 480);
					Chat.this.frame.setLocationRelativeTo(null);

					jpStart.removeAll();
					jpStart.setLayout(new BorderLayout());
					
					jpStart.add(new Server(serverSocket, maxUsersCount), BorderLayout.CENTER);
					jpStart.updateUI();
					
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, e2.getClass().getName() + ": " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				

			}
		});

		//Client UI
		JPanel jpClient = new JPanel(new BorderLayout());
		jpClient.setBorder(new TitledBorder("Client"));
			
		JPanel jpConfigClient = new JPanel(new GridLayout(3, 3, 5, 20));
		jpConfigClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel jlblConnectToIp = new JLabel("Ip Address: ");
		jlblConnectToIp.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblConnectToIp);
		jtfConnectToIp = new JTextField(connectToIP, 7);
		jpConfigClient.add(jtfConnectToIp);
		
		JLabel jlblConnectToPort = new JLabel("Port Number: ");
		jlblConnectToPort.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblConnectToPort);
		jtfConnectToPort = new JTextField(connectToPort, 7);
		jpConfigClient.add(jtfConnectToPort);
		
		JLabel jlblUserName = new JLabel("User Name: ");
		jlblUserName.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblUserName);
		jtfUserName = new JTextField(userName, 7);
		jpConfigClient.add(jtfUserName);
		
		JPanel jpStartClient = new JPanel(new BorderLayout());
		jpStartClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton jbtnStartClient = new JButton("Connect to Server");
		jpStartClient.add(jbtnStartClient, BorderLayout.CENTER);
		
		
		jpClient.add(jpConfigClient, BorderLayout.CENTER);
		jpClient.add(jpStartClient, BorderLayout.SOUTH);
		
		jpStart.add(jpClient);
		
		//start client
		jbtnStartClient.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String ip = jtfConnectToIp.getText();
				if(ip.equals("")) {
					JOptionPane.showMessageDialog(null, "Enter Ip Address", "Error", JOptionPane.ERROR_MESSAGE);
					jtfConnectToIp.requestFocus();
					return;
				}
				
				int port = 0;
				try {
					port = Integer.parseInt(jtfConnectToPort.getText());
					if((port < 1) || (port > 65535)) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(null, "\"Port Number\" must be an integer between 1 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
					jtfConnectToPort.requestFocus();
					return;
				}
				
				String userName = jtfUserName.getText();
				if(userName.equals("")) {
					JOptionPane.showMessageDialog(null, "Enter User Name", "Error", JOptionPane.ERROR_MESSAGE);
					jtfUserName.requestFocus();
					return;
				}

				try {					
					PrintWriter output = new PrintWriter(clientConfigFile, charsetName);
					output.write("ip=" + ip + "\r\nport=" + port + "\r\nusername=" + userName);
					output.close();
				} catch (IOException e2) {
				}
				
				/*
				try {
					InetAddress addr = InetAddress.getByName(ip);
				} catch (UnknownHostException e2) {
					JOptionPane.showMessageDialog(null, e2.getClass().getName() + ": " + e2.getMessage(), "Error1", JOptionPane.ERROR_MESSAGE);
				}*/
			}
		});
		
		add(jpStart, BorderLayout.CENTER);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Chat, created by Viktor Bohush");
		frame.add(new Chat(frame));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setMinimumSize(new Dimension(frame.getWidth(), frame.getHeight()));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
