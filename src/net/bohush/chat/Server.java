package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JPanel implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private JTextArea jtaLog = new JTextArea();
	private List<NewClient> clients = Collections.synchronizedList(new ArrayList<NewClient>());
	private ServerSocket serverSocket;
	private int maxUsersCount;
	
	public Server(ServerSocket serverSocket, int maxUsersCount) {
		this.serverSocket = serverSocket;
		this.maxUsersCount = maxUsersCount;
		setLayout(new BorderLayout());
		jtaLog.setEditable(false);
		jtaLog.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		jtaLog.append(new Date() + " Startig Chat Server\n");
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
				NewClient newClient = new NewClient(socket);
				clients.add(newClient);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class NewClient implements Runnable {
		private Socket socket;
		private String userName = "";
		private PrintWriter toClient;
		
		public NewClient(Socket socket) {
			this.socket = socket;
			Thread thread = new Thread(this);
			thread.start();
		}
		
		public void run() {
			try {
				Scanner fromClient = new Scanner(socket.getInputStream(), Chat.charsetName);
				toClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Chat.charsetName));
				userName = fromClient.nextLine();
				
				//check max users count
				if(clients.size() > maxUsersCount) {
					toClient.println("2");
					toClient.flush();
					fromClient.close();
					toClient.close();
					clients.remove(this);
					synchronized (jtaLog) {
						jtaLog.append(new Date() + " Disallow connection from  " + socket + ", user name \""+ userName + "\", too many users\n");	
						jtaLog.append(new Date() + " Clients = " + clients.size() + "\n");
						jtaLog.setCaretPosition(jtaLog.getDocument().getLength());
					}	
					return;
				}
				
				//check duplicate user name
				synchronized (clients) {
					for (NewClient newClient : clients) {
						if((newClient !=  this)&&(newClient.getUserName().equals(userName))) {
							toClient.println("1");
							toClient.flush();
							fromClient.close();
							toClient.close();
							synchronized (jtaLog) {
								clients.remove(this);
								jtaLog.append(new Date() + " Disallow connection from  " + socket + ", duplicate user name \""+ userName + "\"\n");	
								jtaLog.append(new Date() + " Clients = " + clients.size() + "\n");
								jtaLog.setCaretPosition(jtaLog.getDocument().getLength());
							}	
							return;
						}
					}						
				}
				
				//Ok, connect
				toClient.println("0");
				toClient.flush();
				
				//tell other clients about new user
				Date timeConnected = new Date();
				synchronized (jtaLog) {
					jtaLog.append(timeConnected + " Connection from  " + socket + ", user name \""+ userName + "\"\n");
					jtaLog.append(timeConnected + " Clients = " + clients.size() + "\n");
					jtaLog.setCaretPosition(jtaLog.getDocument().getLength());
				}

				//list of clients
				StringBuilder users = new StringBuilder();
				synchronized (clients) {
					for (NewClient newClient : clients) {
						users.append(newClient.getUserName() + "\n");
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
						synchronized (jtaLog) {
							jtaLog.append(new Date() + " " + userName + ": " + text + "\n");
							jtaLog.setCaretPosition(jtaLog.getDocument().getLength());
						}
						synchronized (clients) {
							for (NewClient newClient : clients) {
								newClient.sendMessages(fontStyle, messageColor, " " + userName + ": " + text);
							}						
						}
					//private message
					} else {
						String toUser = fromClient.nextLine();
						String fontStyle = fromClient.nextLine();
						String messageColor = fromClient.nextLine();
						String text = fromClient.nextLine();
						synchronized (jtaLog) {
							jtaLog.append(new Date() + " private message from " + userName + " to " + toUser + "\n");
							jtaLog.setCaretPosition(jtaLog.getDocument().getLength());
						}
						synchronized (clients) {
							for (NewClient newClient : clients) {
								if((newClient.getUserName().equals(toUser)) || (newClient.getUserName().equals(userName))) {
									newClient.sendPrivateMessages(fontStyle, messageColor, " " + userName + " > " + toUser + ": " + text);
								}
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
				synchronized (jtaLog) {
					jtaLog.append(timeLeaved + " Disconnect client " + socket + ", user name \""+ userName + "\"\n");
					jtaLog.append(timeLeaved + " Clients = " + clients.size() + "\n");
					jtaLog.setCaretPosition(jtaLog.getDocument().getLength());
				}
				
				//list of clients
				StringBuilder users = new StringBuilder();
				synchronized (clients) {
					for (NewClient newClient : clients) {
						users.append(newClient.getUserName() + "\n");
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
