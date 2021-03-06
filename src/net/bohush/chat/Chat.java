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
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private JCheckBox jcbNonServerMode;
	private JLabel jlblConnectToIp;
	private JTextField jtfConnectToIp;
	private JTextField jtfConnectToPort;
	private JTextField jtfUserName;
	private JButton jbtnStartClient;
	private JButton jbtnStartServer;
	
	private JFrame frame;
	
	private JPanel jpStart = new JPanel(new GridLayout(1, 2, 5, 5));
	private File serverConfigFile;
	private File clientConfigFile;
	static String charsetName = StandardCharsets.UTF_8.name();

	//need for saving client settings
	private boolean isNonServerMode = false;
	private boolean isFontBold = false;
	private boolean isFontItalic = false;
	private String fontColor = Color.BLACK.getRGB() + "";
	private String clientSettings = "";
	private Client client;
	
	private boolean isAdmin = false;
	//need for saving server settings
	private String stringAdmins = "";
	private Map<String, String> admins = new HashMap<String, String>();
	
	private boolean isStarting = false;
	
	public Chat(JFrame frame)  {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {			
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		} catch (UnsupportedLookAndFeelException e1) {
		}
		
		
		this.frame = frame;
		
		String serverConfigFileName = this.getClass().getResource("/").getPath() + "server.txt";
		String clientConfigFileName = this.getClass().getResource("/").getPath() + "client.txt";
		
		try {
			serverConfigFile = new File(URLDecoder.decode(serverConfigFileName, charsetName));
		} catch (UnsupportedEncodingException e3) { }
		
		try {
			clientConfigFile = new File(URLDecoder.decode(clientConfigFileName, charsetName));
		} catch (UnsupportedEncodingException e3) { }

		//server config
		String serverPort = "2014";
		String maxUsersCount = "5";
		if(serverConfigFile.exists()) {
			Scanner serverInput;
			try {
				serverInput = new Scanner(serverConfigFile, charsetName);
				HashMap<String, String> configs = new HashMap<String, String>();
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
				if(configs.get("admins") != null) {
					stringAdmins = configs.get("admins");
					String[] adminsWithPass = stringAdmins.split(",");
					int adminCount = adminsWithPass.length;
					for (int i = 0; i < adminCount; i++) {
						String[] oneAdmin = adminsWithPass[i].split(":");
						if(oneAdmin.length == 2) {
							admins.put(oneAdmin[0].toLowerCase(), oneAdmin[1]);
						}						
					}
				}
			} catch (IOException e) {
			}
		} else {
			stringAdmins = "admin:chatpass,viktor:12345678";
			admins.put("admin", "chatpass");
			admins.put("viktor", "12345678");
		}
		
		//client config
		String connectToIP = "localhost";
		String connectToPort = "2014";
		String userName = "User";
		if(clientConfigFile.exists()) {
			Scanner clientInput;
			try {
				clientInput = new Scanner(clientConfigFile, charsetName);
				HashMap<String, String> configs = new HashMap<String, String>();
				while(clientInput.hasNextLine()) {
					String nextLine = clientInput.nextLine();
					String[] params = nextLine.split("=");
					if(params.length > 1) {
						configs.put(params[0].toLowerCase(), params[1]);	
					}				
				}
				clientInput.close();
				if((configs.get("isnonservermode") != null) && (configs.get("isnonservermode").equals("true"))) {
					isNonServerMode = true;			
				}
				if(configs.get("ip") != null) {
					connectToIP = configs.get("ip");
				}
				if(configs.get("port") != null) {
					connectToPort = configs.get("port");					
				}
				if(configs.get("username") != null) {
					userName = configs.get("username");			
				}
				if((configs.get("isfontbold") != null) && (configs.get("isfontbold").equals("true"))) {
					isFontBold = true;			
				}				
				if((configs.get("isfontitalic") != null) && (configs.get("isfontitalic").equals("true"))) {
					isFontItalic = true;			
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
		
		JPanel jpConfigServer = new JPanel(new GridLayout(4, 2, 5, 20));
		jpConfigServer.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		jpConfigServer.add(new JLabel(" "));
		jpConfigServer.add(new JLabel(" "));
		
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
		jbtnStartServer = new JButton("Start Server");
		jbtnStartServer.setPreferredSize(new Dimension(30, 30));
		jpStartServer.add(jbtnStartServer, BorderLayout.CENTER);		
		
		jpServer.add(jpConfigServer, BorderLayout.CENTER);
		jpServer.add(jpStartServer, BorderLayout.SOUTH);
		
		jpStart.add(jpServer);
		
		class StartingServerThread implements Runnable {
			@Override
			public void run() {
				jbtnStartServer.setText("Starting...");
				jtfServerPort.setEnabled(false);
				jtfServerCount.setEnabled(false);
				jcbNonServerMode.setEnabled(false);
				jtfConnectToIp.setEnabled(false);
				jtfConnectToPort.setEnabled(false);
				jtfUserName.setEnabled(false);
				jbtnStartClient.setEnabled(false);
				jbtnStartServer.setEnabled(false);
				
				//check port
				int port = 0;
				try {
					port = Integer.parseInt(jtfServerPort.getText());
					if((port < 1) || (port > 65535)) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(null, "\"Port Number\" must be an integer between 1 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
					finishThread();
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
					finishThread();
					jtfServerCount.requestFocus();
					return;
				}
				//safe settings
				try {					
					PrintWriter output = new PrintWriter(serverConfigFile, charsetName);
					output.write("port=" + port + "\r\nmaxuserscount=" + maxUsersCount + "\r\nadmins=" + stringAdmins);
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
					
					Server server = new Server(Chat.this.admins, serverSocket, maxUsersCount); 
					jpStart.removeAll();
					jpStart.setLayout(new BorderLayout());					
					jpStart.add(server, BorderLayout.CENTER);
					jpStart.updateUI();
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, e2.getClass().getName() + ": " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				finishThread();
				jtfServerPort.requestFocus();
			}
			
			private void finishThread() {
				jbtnStartServer.setText("Start Server");
				jtfServerPort.setEnabled(true);
				jtfServerCount.setEnabled(true);
				jcbNonServerMode.setEnabled(true);
				jtfConnectToIp.setEnabled(true);
				jtfConnectToPort.setEnabled(true);
				jtfUserName.setEnabled(true);
				jbtnStartClient.setEnabled(true);
				jbtnStartServer.setEnabled(true);
				updateClientConfigUI();
				isStarting = false;
			}
			
		}
		
		//start server
		ActionListener startServerAction = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!isStarting) {
					isStarting = true;
					Thread thread = new Thread(new StartingServerThread());
					thread.start();
				}				
			}
		};
		jtfServerPort.addActionListener(startServerAction);
		jtfServerCount.addActionListener(startServerAction);
		jbtnStartServer.addActionListener(startServerAction);

		//Client UI
		JPanel jpClient = new JPanel(new BorderLayout());
		jpClient.setBorder(new TitledBorder("Client"));
			
		JPanel jpConfigClient = new JPanel(new GridLayout(4, 2, 5, 20));
		jpConfigClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JLabel jlblNonServerMode = new JLabel("Non-Server Mode: ");
		jlblNonServerMode.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblNonServerMode);
		jcbNonServerMode = new JCheckBox("", isNonServerMode);
		jcbNonServerMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isNonServerMode = jcbNonServerMode.isSelected();
				updateClientConfigUI();
			}
		});
		jpConfigClient.add(jcbNonServerMode);
		
		jlblConnectToIp = new JLabel("IP Address: ");
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
		jbtnStartClient = new JButton("Start Client");
		jbtnStartClient.setPreferredSize(new Dimension(30, 30));
		jpStartClient.add(jbtnStartClient, BorderLayout.CENTER);
		
		
		jpClient.add(jpConfigClient, BorderLayout.CENTER);
		jpClient.add(jpStartClient, BorderLayout.SOUTH);

		updateClientConfigUI();
		jpStart.add(jpClient);
		
		class StartingClientThread implements Runnable {

			@Override
			public void run() {
				jbtnStartClient.setText("Starting...");
				jtfServerPort.setEnabled(false);
				jtfServerCount.setEnabled(false);
				jcbNonServerMode.setEnabled(false);
				jtfConnectToIp.setEnabled(false);
				jtfConnectToPort.setEnabled(false);
				jtfUserName.setEnabled(false);
				jbtnStartClient.setEnabled(false);
				jbtnStartServer.setEnabled(false);
				
				//check ip
				String ip = jtfConnectToIp.getText();				
				if(!jcbNonServerMode.isSelected()) {
					if(ip.equals("")) {
						JOptionPane.showMessageDialog(null, "Enter IP address", "Error", JOptionPane.ERROR_MESSAGE);
						finishThread();
						jtfConnectToIp.requestFocus();
						return;
					}			
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
					finishThread();
					jtfConnectToPort.requestFocus();
					return;
				}
				//check user name
				String userName = jtfUserName.getText();
				if(userName.equals("")) {
					JOptionPane.showMessageDialog(null, "Enter user name", "Error", JOptionPane.ERROR_MESSAGE);
					finishThread();
					jtfUserName.requestFocus();
					return;
				}
				clientSettings = "isnonservermode=" + isNonServerMode + "\r\nip=" + ip + "\r\nport=" + port + "\r\nusername=" + userName + "\r\n";
				//save settings
				saveClientSettings(clientSettings + "isfontbold=" + isFontBold + "\r\nisfontitalic=" + isFontItalic + "\r\nfontcolor=" + fontColor);
				
				if(jcbNonServerMode.isSelected()) {
					try {
						DatagramSocket serverSocket = new DatagramSocket(port);
						
						Chat.this.frame.setSize(640, 480);
						Chat.this.frame.setLocationRelativeTo(null);
						Chat.this.frame.setTitle(Chat.this.frame.getTitle() + ", started in non-server mode on port " + port + " as " + userName);
						Chat.this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						
						client = new Client(true, null, null, serverSocket, port, userName, isFontBold, isFontItalic, fontColor, isAdmin);
						
						Chat.this.frame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent e) {
								int confirm = JOptionPane.showOptionDialog( null, "Are you sure you want to exit?",
								"Exit confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
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
						
					} catch (SocketException e) {
						JOptionPane.showMessageDialog(null, e.getClass().getName() + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);;
					}
					finishThread();
					jtfUserName.requestFocus();
				} else {
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
							finishThread();
							jtfUserName.requestFocus();
							return;
						} else if(answer.equals("2")) {
							JOptionPane.showMessageDialog(null, "There are too many users logged in", "Error", JOptionPane.ERROR_MESSAGE);
							toServer.close();
							fromServer.close();
							socket.close();
							finishThread();
							jtfUserName.requestFocus();
							return;
						} else if(answer.equals("4")) {
							JOptionPane.showMessageDialog(null, "Your IP is banned", "Error", JOptionPane.ERROR_MESSAGE);
							toServer.close();
							fromServer.close();
							socket.close();
							finishThread();
							jtfUserName.requestFocus();
							return;
						} else {
							if(answer.equals("3")) {
								String password = JOptionPane.showInputDialog(null, "Enter your passwrod", "Admin login", JOptionPane.QUESTION_MESSAGE);
								if(password == null) {
									password = "";
								}
								toServer.println(password);
								toServer.flush();
								String allow = fromServer.nextLine();
								if(allow.equals("1")) {
									JOptionPane.showMessageDialog(null, "Wrong password", "Error", JOptionPane.ERROR_MESSAGE);
									toServer.close();
									fromServer.close();
									socket.close();
									finishThread();
									jtfUserName.requestFocus();
									return;
								} else {
									isAdmin = true;
								}
							}
							Chat.this.frame.setSize(640, 480);
							Chat.this.frame.setLocationRelativeTo(null);
							Chat.this.frame.setTitle(Chat.this.frame.getTitle() + ", connected to " + ip + ":" + port + " as " + userName);
							Chat.this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
							
							client = new Client(false, toServer, fromServer, null, port, userName, isFontBold, isFontItalic, fontColor, isAdmin);
							
							Chat.this.frame.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent e) {
									int confirm = JOptionPane.showOptionDialog( null, "Are you sure you want to disconnect and exit?",
									"Exit confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
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
					finishThread();
					jtfUserName.requestFocus();
				}
			}
			
			private void finishThread() {
				jbtnStartClient.setText("Start Client");
				jtfServerPort.setEnabled(true);
				jtfServerCount.setEnabled(true);
				jcbNonServerMode.setEnabled(true);
				jtfConnectToIp.setEnabled(true);
				jtfConnectToPort.setEnabled(true);
				jtfUserName.setEnabled(true);
				jbtnStartClient.setEnabled(true);
				jbtnStartServer.setEnabled(true);
				updateClientConfigUI();
				isStarting = false;
			}
			
		}
		
		//start client
		ActionListener startClientAction = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!isStarting) {
					isStarting = true;
					Thread thread = new Thread(new StartingClientThread());
					thread.start();
				}
			}
		};
		jtfConnectToIp.addActionListener(startClientAction);
		jtfConnectToPort.addActionListener(startClientAction);
		jtfUserName.addActionListener(startClientAction);
		jbtnStartClient.addActionListener(startClientAction);
		
		add(jpStart, BorderLayout.CENTER);
	}
	
	private void updateClientConfigUI() {
		if(isNonServerMode) {
			jtfConnectToIp.setEnabled(false);
			jlblConnectToIp.setEnabled(false);
		} else {
			jtfConnectToIp.setEnabled(true);
			jlblConnectToIp.setEnabled(true);
		}
	}
	
	public void saveClientSettings(String settings) {
		try {					
			PrintWriter output = new PrintWriter(clientConfigFile, charsetName);
			output.write(settings);
			output.close();
		} catch (IOException e2) {
			JOptionPane.showMessageDialog(null, e2.getClass().getName() + ": " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
