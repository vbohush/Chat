package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;

public class Server extends JPanel implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private DefaultStyledDocument doc = new DefaultStyledDocument();
	private JTextArea jtaLog = new JTextArea(doc);
	private Set<NewClient> clients = Collections.synchronizedSet(new TreeSet<NewClient>());
	private ServerSocket serverSocket;
	private int maxUsersCount;
	private Map<String, String> admins;
	
	String banlistFileName = this.getClass().getResource("/").getPath() + "banlist.txt";
	RandomAccessFile banlistFile;
	private Set<String> banlist = Collections.synchronizedSet(new HashSet<String>());
	
	public Server(Map<String, String> admins, ServerSocket serverSocket, int maxUsersCount) {
		this.admins = admins;
		this.serverSocket = serverSocket;
		this.maxUsersCount = maxUsersCount;
		
		//load list of banned ips
		try {
			banlistFileName = URLDecoder.decode(banlistFileName, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e3) { }
		try {
			banlistFile = new RandomAccessFile(banlistFileName, "rw");
			try {
				String nextIp = banlistFile.readLine();
				while(nextIp != null) {
					if(!nextIp.equals("")) {
						banlist.add(nextIp);
					}
					nextIp = banlistFile.readLine();
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e1) {
		}
		
		setLayout(new BorderLayout());
		jtaLog.setEditable(false);
		jtaLog.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		try {
			doc.insertString(doc.getLength(), new Date() + " Startig Chat Server\n", new SimpleAttributeSet());
		} catch (BadLocationException e) {}
	    JScrollPane jsp = new JScrollPane(jtaLog);
	    
        add(jsp, BorderLayout.CENTER);
	    
	    Thread thread = new Thread(this);
	    thread.start();
	}
	
	//accept new clients
	@Override
	public void run() {
		try {
			while(true) {
				Socket socket = serverSocket.accept();
				new NewClient(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class NewClient implements Runnable, Comparable<NewClient>  {
		private Socket socket;
		private String userName = "";
		private String ip = "";
		private PrintWriter toClient;
		
		public NewClient(Socket socket) {
			this.socket = socket;
			Thread thread = new Thread(this);
			thread.start();
		}
		
		@Override
		public int compareTo(NewClient o) {
			return this.userName.compareTo(o.userName);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NewClient) {
				NewClient that = (NewClient)obj;
				return this.userName.toLowerCase().equals(that.userName.toLowerCase());
			} else {
				return false;
			}
		}
		
		
		
		@Override
		public int hashCode() {
			return userName.hashCode();
		}
		
		private void saveToLog(String logText){
			synchronized (doc) {
				try {
					doc.insertString(doc.getLength(), logText, new SimpleAttributeSet());
					while(doc.getText(0, doc.getLength()).split("\n").length > 1000) {
						doc.remove(0, doc.getText(0, doc.getLength()).indexOf("\n") + 1);
					}
				} catch (BadLocationException e) {}
					
				
				jtaLog.setCaretPosition(doc.getLength());
			}
		}
		
		public void run() {
			try {
				Scanner fromClient = new Scanner(socket.getInputStream(), Chat.charsetName);
				toClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Chat.charsetName));
				userName = fromClient.nextLine();
				ip = socket.getInetAddress().getHostAddress();


				//check max users count
				if(banlist.contains(ip)) {
					toClient.println("4");
					toClient.flush();
					fromClient.close();
					toClient.close();
					saveToLog(new Date() + " Disallow connection from  " + socket + ", user name \""+ userName + "\", ip " + ip + " is banned\n");
					return;
				}
				
				//check max users count
				if(clients.size() >= maxUsersCount) {
					toClient.println("2");
					toClient.flush();
					fromClient.close();
					toClient.close();
					saveToLog(new Date() + " Disallow connection from  " + socket + ", user name \""+ userName + "\", too many users\n");
					return;
				}
				
				//check duplicate user name
				if(clients.contains(this)) {
					toClient.println("1");
					toClient.flush();
					fromClient.close();
					toClient.close();
					saveToLog(new Date() + " Disallow connection from  " + socket + ", duplicate user name \""+ userName + "\"\n");
					return;
				}						
				
				//check admins
				if(admins.containsKey(userName.toLowerCase())) {
					toClient.println("3");
					toClient.flush();
					String password = fromClient.nextLine();
					if(password.equals(admins.get(userName.toLowerCase()))) {
						toClient.println("0");
						toClient.flush();		
					} else {
						toClient.println("1");
						toClient.flush();
						fromClient.close();
						toClient.close();
						saveToLog(new Date() + " Disallow connection from  " + socket + ", wrong password from \""+ userName + "\"\n");
						return;
					}
				} else {
					toClient.println("0");
					toClient.flush();
				}

				//Ok, connected
				clients.add(this);
				
				//tell other clients about new user
				Date timeConnected = new Date();
				saveToLog(timeConnected + " Connection from  " + socket + ", user name \""+ userName + "\"\n" + timeConnected + " Clients = " + clients.size() + "\n");


				//list of clients
				StringBuilder users = new StringBuilder();
				synchronized (clients) {
					for (NewClient newClient : clients) {
						users.append(newClient.getUserName() + "\n");
						users.append(newClient.getIp() + "\n");
					}						
				}
				//send clients names
				synchronized (clients) {
					for (NewClient newClient : clients) {
						newClient.sendMessages(" -= " + userName + " entered the chat =-");
						newClient.sendClients(clients.size(), users.toString());
					}						
				}
		
				//accept new messages from client
				while(true) {
					String command = fromClient.nextLine();
					//to all users
					if(command.equals("1")) {
						String fontStyle = fromClient.nextLine();
						String messageColor = fromClient.nextLine();
						String text = fromClient.nextLine();

						saveToLog(new Date() + " " + userName + ": " + text + "\n");

						synchronized (clients) {
							for (NewClient newClient : clients) {
								newClient.sendMessages(fontStyle, messageColor, " " + userName + ": " + text);
							}						
						}
					//private message
					} else if(command.equals("2")){
						String toUser = fromClient.nextLine();
						String fontStyle = fromClient.nextLine();
						String messageColor = fromClient.nextLine();
						String text = fromClient.nextLine();
						saveToLog(new Date() + " private message from " + userName + " to " + toUser + "\n");
						synchronized (clients) {
							for (NewClient newClient : clients) {
								if((newClient.getUserName().equals(toUser)) || (newClient.getUserName().equals(userName))) {
									newClient.sendPrivateMessages(fontStyle, messageColor, " " + userName + " > " + toUser + ": " + text);
								}
							}						
						}
					//ban users
					} else {
						String ipToBan = fromClient.nextLine();
						saveToLog(new Date() + " " + ipToBan + " is banned\n");
						
						if(!banlist.contains(ipToBan)) {
							banlistFile.writeBytes(ipToBan + "\r\n");
							banlist.add(ipToBan);							
						}
						
						ArrayList<NewClient> usersToBan = new ArrayList<NewClient>();
						
						synchronized (clients) {
							for (NewClient newClient : clients) {
								if(newClient.getIp().equals(ipToBan)) {
									usersToBan.add(newClient);
								}
							}
							for (NewClient newClient : usersToBan) {
								newClient.disConnect();
								clients.remove(newClient);
							}
							//list of clients
							users = new StringBuilder();
							for (NewClient newClient : clients) {
								users.append(newClient.getUserName() + "\n");
								users.append(newClient.getIp() + "\n");
							}						
							//send clients names
							for (NewClient newClient : clients) {
								newClient.sendClients(clients.size(), users.toString());
							}
						}

					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				//disconnect
				clients.remove(this);
				Date timeLeaved = new Date();
				saveToLog(timeLeaved + " Disconnect client " + socket + ", user name \""+ userName + "\"\n" + timeLeaved + " Clients = " + clients.size() + "\n");
				
				//list of clients
				StringBuilder users = new StringBuilder();
				synchronized (clients) {
					for (NewClient newClient : clients) {
						users.append(newClient.getUserName() + "\n");
						users.append(newClient.getIp() + "\n");
					}						
				}
				
				synchronized (clients) {
					for (NewClient newClient : clients) {
						newClient.sendMessages(" -= " + userName + " leaved the chat =-");
						newClient.sendClients(clients.size(), users.toString());
					}						
				}

			}
		}
		
		public String getUserName() {
			return userName;
		}
		
		public String getIp() {
			return ip;
		}

		public void disConnect() {
			toClient.println("4");
			toClient.flush();		
		}
		
		public void sendClients(int size, String text) {
			toClient.println("2");
			toClient.println(size + "");
			toClient.println(text);
			toClient.flush();
		}

		public void sendMessages(String message) {
			String messageFontStyle = Font.BOLD + "";
			String messageColor = Color.GRAY.getRGB() + "";
			sendMessages(messageFontStyle, messageColor, message);
		}
		
		public void sendMessages(String messageFontStyle, String messageColor, String message) {
			toClient.println("1");
			toClient.println(messageFontStyle);
			toClient.println(messageColor);
			toClient.println(message);
			toClient.flush();
		}
		
		public void sendPrivateMessages(String messageFontStyle, String messageColor, String message) {
			toClient.println("3");
			toClient.println(messageFontStyle);
			toClient.println(messageColor);
			toClient.println(message);
			toClient.flush();
		}
	}

}
