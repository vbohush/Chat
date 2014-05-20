package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
	static String charsetName = StandardCharsets.UTF_8.name();
	
	//nedd for saving client settings
	private String isFontBold = "n";
	private String isFontItalic = "n";
	private String fontColor = Color.BLACK.getRGB() + "";
	private String clientSettings = "";
	private Client client;
	
	public Chat(JFrame frame)  {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {}
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
		String connectToIP = "localhost";
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
				if((configs.get("isfontbold") != null) && (configs.get("isfontbold").equals("y"))) {
					isFontBold = "y";			
				}
				if((configs.get("isfontitalic") != null) && (configs.get("isfontitalic").equals("y"))) {
					isFontItalic = "y";			
				}
				if(configs.get("fontcolor") != null) {
					fontColor = configs.get("fontcolor");					
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
		jtfServerPort = new JTextFieldLimit(serverPort, 7, 5);
		jpConfigServer.add(jtfServerPort);
		
		JLabel jlblServerCount = new JLabel("Max Users Count: ");
		jlblServerCount.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigServer.add(jlblServerCount);
		jtfServerCount = new JTextFieldLimit(maxUsersCount, 7, 5);
		jpConfigServer.add(jtfServerCount);
		
		jpConfigServer.add(new JLabel(" "));
		jpConfigServer.add(new JLabel(" "));
		
		JPanel jpStartServer = new JPanel(new BorderLayout());
		jpStartServer.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton jbtnStartServer = new JButton("Start Server");
		jbtnStartServer.setPreferredSize(new Dimension(30, 30));
		jpStartServer.add(jbtnStartServer, BorderLayout.CENTER);		
		
		jpServer.add(jpConfigServer, BorderLayout.CENTER);
		jpServer.add(jpStartServer, BorderLayout.SOUTH);
		
		jpStart.add(jpServer);
		
		//start server
		ActionListener startServerAction = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				//check port
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
				//check max users count
				int maxUsersCount = 0;
				try {
					maxUsersCount = Integer.parseInt(jtfServerCount.getText());
					if(maxUsersCount < 2) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(null, "\"Max Users Count\" must be an integer greater than 1", "Error", JOptionPane.ERROR_MESSAGE);
					jtfServerCount.requestFocus();
					return;
				}
				//safe settings
				try {					
					PrintWriter output = new PrintWriter(serverConfigFile, charsetName);
					output.write("port=" + port + "\r\nmaxuserscount=" + maxUsersCount);
					output.close();
				} catch (IOException e2) {
				}
				//start server
				try {
					ServerSocket serverSocket = new ServerSocket(port);

					Chat.this.frame.setSize(640, 480);
					Chat.this.frame.setLocationRelativeTo(null);
					Chat.this.frame.setTitle(Chat.this.frame.getTitle() + ", started at port " + port + " with max users count " + maxUsersCount);
					Chat.this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					Chat.this.frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							int confirm = JOptionPane.showOptionDialog( null, "Are you sure you want to stop the server and exit?",
							"Exit confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
							if (confirm == JOptionPane.YES_OPTION) {
								System.exit(0);
							}
						}
					});
					
					Server server = new Server(serverSocket, maxUsersCount); 
					jpStart.removeAll();
					jpStart.setLayout(new BorderLayout());					
					jpStart.add(server, BorderLayout.CENTER);
					jpStart.updateUI();
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, e2.getClass().getName() + ": " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		};
		jtfServerPort.addActionListener(startServerAction);
		jtfServerCount.addActionListener(startServerAction);
		jbtnStartServer.addActionListener(startServerAction);

		//Client UI
		JPanel jpClient = new JPanel(new BorderLayout());
		jpClient.setBorder(new TitledBorder("Client"));
			
		JPanel jpConfigClient = new JPanel(new GridLayout(3, 3, 5, 20));
		jpConfigClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel jlblConnectToIp = new JLabel("IP Address: ");
		jlblConnectToIp.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblConnectToIp);
		jtfConnectToIp = new JTextField(connectToIP, 7);
		jpConfigClient.add(jtfConnectToIp);
		
		JLabel jlblConnectToPort = new JLabel("Port Number: ");
		jlblConnectToPort.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblConnectToPort);
		jtfConnectToPort = new JTextFieldLimit(connectToPort, 7, 5);
		jpConfigClient.add(jtfConnectToPort);
		
		JLabel jlblUserName = new JLabel("User Name: ");
		jlblUserName.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblUserName);
		jtfUserName = new JTextFieldLimit(userName, 7, 32);
		jpConfigClient.add(jtfUserName);
		
		JPanel jpStartClient = new JPanel(new BorderLayout());
		jpStartClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton jbtnStartClient = new JButton("Connect to Server");
		jbtnStartClient.setPreferredSize(new Dimension(30, 30));
		jpStartClient.add(jbtnStartClient, BorderLayout.CENTER);
		
		
		jpClient.add(jpConfigClient, BorderLayout.CENTER);
		jpClient.add(jpStartClient, BorderLayout.SOUTH);
		
		jpStart.add(jpClient);
		
		//start client
		ActionListener startClientAction = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				//check ip
				String ip = jtfConnectToIp.getText();
				if(ip.equals("")) {
					JOptionPane.showMessageDialog(null, "Enter IP address", "Error", JOptionPane.ERROR_MESSAGE);
					jtfConnectToIp.requestFocus();
					return;
				}
				//check port
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
				//check user name
				String userName = jtfUserName.getText();
				if(userName.equals("")) {
					JOptionPane.showMessageDialog(null, "Enter user name", "Error", JOptionPane.ERROR_MESSAGE);
					jtfUserName.requestFocus();
					return;
				}
				clientSettings = "ip=" + ip + "\r\nport=" + port + "\r\nusername=" + userName + "\r\n";
				//save settings
				saveClientSettings(clientSettings + "isfontbold=" + isFontBold + "\r\nisfontitalic=" + isFontItalic + "\r\nfontcolor=" + fontColor);
				
				//connect to server
				try {
					@SuppressWarnings("resource")
					Socket socket = new Socket(ip, port);
					
					PrintWriter toServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), charsetName));
					toServer.println(userName);
					toServer.flush();
					
					Scanner fromServer = new Scanner(socket.getInputStream(), charsetName);
					String answer = fromServer.nextLine();
					if(answer.equals("1")) {
						JOptionPane.showMessageDialog(null, "User \"" + userName + "\" is already logged in", "Error", JOptionPane.ERROR_MESSAGE);
						toServer.close();
						fromServer.close();
						socket.close();
						jtfUserName.requestFocus();
						return;
					} if(answer.equals("2")) {
						JOptionPane.showMessageDialog(null, "There are too many users logged in", "Error", JOptionPane.ERROR_MESSAGE);
						toServer.close();
						fromServer.close();
						socket.close();
						jtfUserName.requestFocus();
						return;
					} else {

						Chat.this.frame.setSize(640, 480);
						Chat.this.frame.setLocationRelativeTo(null);
						Chat.this.frame.setTitle(Chat.this.frame.getTitle() + ", connected to " + ip + ":" + port + " as " + userName);
						Chat.this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						
						client = new Client(toServer, fromServer, isFontBold, isFontItalic, fontColor);
						
						Chat.this.frame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent e) {
								int confirm = JOptionPane.showOptionDialog( null, "Are you sure you want to disconnect and exit?",
								"Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
								if (confirm == JOptionPane.YES_OPTION) {
									Chat.this.saveClientSettings(clientSettings + Chat.this.client.getSettings());
									System.exit(0);
								}
							}
						});
						
						jpStart.removeAll();
						jpStart.setLayout(new BorderLayout());
						jpStart.add(client, BorderLayout.CENTER);
						client.setFocus();
						jpStart.updateUI();

					}					
				} catch (UnknownHostException e2) {
					JOptionPane.showMessageDialog(null, "Unknown host: \"" + ip + "\"", "Error", JOptionPane.ERROR_MESSAGE);
					jtfConnectToIp.requestFocus();
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, e2.getClass().getName() + ": " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} 

			}
		};
		jtfConnectToIp.addActionListener(startClientAction);
		jtfConnectToPort.addActionListener(startClientAction);
		jtfUserName.addActionListener(startClientAction);
		jbtnStartClient.addActionListener(startClientAction);
		
		add(jpStart, BorderLayout.CENTER);
	}
	
	public void saveClientSettings(String settings) {
		try {					
			PrintWriter output = new PrintWriter(clientConfigFile, charsetName);
			output.write(settings);
			output.close();
		} catch (IOException e2) {
		}
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
