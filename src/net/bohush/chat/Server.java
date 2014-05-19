package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
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
	    
		jsp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}
		});
	    
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
							}	
							return;
						}
					}						
				}
				
				//Ok, connect
				toClient.println("0");
				toClient.flush();
				
				//tell other clients about new user
				Calendar timeConnected = new GregorianCalendar();
				synchronized (jtaLog) {
					jtaLog.append(timeConnected.getTime().toString() + " Connection from  " + socket + ", user name \""+ userName + "\"\n");
					jtaLog.append(timeConnected.getTime().toString() + " Clients = " + clients.size() + "\n");
				}
				String userEnteredTime = String.format("[%02d:%02d:%02d]", timeConnected.get(Calendar.HOUR_OF_DAY), timeConnected.get(Calendar.MINUTE), timeConnected.get(Calendar.SECOND));
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
						newClient.sendMessages(userEnteredTime, " -= " + userName + " entered the chat =-");
						newClient.sendClients(clients.size(), users.toString());
					}						
				}
		
				//accept new messages from client
				while(true) {
					String fontStyle = fromClient.nextLine();
					String messageColor = fromClient.nextLine();

					String text = fromClient.nextLine();
					Calendar time = new GregorianCalendar();
					synchronized (jtaLog) {
						jtaLog.append(time.getTime().toString() + " " + userName + ": " + text + "\n");	
					}
					String messageTime = String.format("[%02d:%02d:%02d]", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.SECOND));
					synchronized (clients) {
						for (NewClient newClient : clients) {
							newClient.sendMessages(messageTime, fontStyle, messageColor, " " + userName + ": " + text);
						}						
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				//disconnect
				clients.remove(this);
				Calendar timeLeaved = new GregorianCalendar();
				synchronized (jtaLog) {
					jtaLog.append(timeLeaved.getTime().toString() + " Disconnect client " + socket + ", user name \""+ userName + "\"\n");
					jtaLog.append(timeLeaved.getTime().toString() + " Clients = " + clients.size() + "\n");
				}
				String userLeavedTime = String.format("[%02d:%02d:%02d]", timeLeaved.get(Calendar.HOUR_OF_DAY), timeLeaved.get(Calendar.MINUTE), timeLeaved.get(Calendar.SECOND));
				
				//list of clients
				StringBuilder users = new StringBuilder();
				synchronized (clients) {
					for (NewClient newClient : clients) {
						users.append(newClient.getUserName() + "\n");
					}						
				}
				
				synchronized (clients) {
					for (NewClient newClient : clients) {
						newClient.sendMessages(userLeavedTime, " -= " + userName + " leaved the chat =-");
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
		
		public void sendMessages(String time, String messageFontStyle, String messageColor, String message) {
			String timeFontStyle = Font.BOLD + "";
			String TimeColor = Color.GRAY.getRGB() + "";
			sendMessages(timeFontStyle, TimeColor, time, messageFontStyle, messageColor, message);
		}
		
		public void sendMessages(String time, String message) {
			String timeFontStyle = Font.BOLD + "";
			String TimeColor = Color.GRAY.getRGB() + "";
			sendMessages(timeFontStyle, TimeColor, time, timeFontStyle, TimeColor, message);
		}
		
		public void sendMessages(String timeFontStyle, String TimeColor, String time, String messageFontStyle, String messageColor, String message) {
			toClient.println("1");
			toClient.println(timeFontStyle);
			toClient.println(TimeColor);
			toClient.println(time);
			toClient.println(messageFontStyle);
			toClient.println(messageColor);
			toClient.println(message);
			toClient.flush();
		}
		
	}

}
