package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
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
				Scanner fromClient = new Scanner(socket.getInputStream());
				toClient = new PrintWriter(socket.getOutputStream());				
				userName = fromClient.nextLine();
				
				if(clients.size() > maxUsersCount) {
					toClient.println("2");
					toClient.flush();
					fromClient.close();
					toClient.close();
					synchronized (jtaLog) {
						clients.remove(this);
						jtaLog.append(new Date() + " Disallow connection from  " + socket + ", user name \""+ userName + "\", too many users\n");	
						jtaLog.append(new Date() + " Clients = " + clients.size() + "\n");
					}	
					return;
				}
				
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
				
				
				toClient.println("0");
				toClient.flush();
				synchronized (jtaLog) {
					jtaLog.append(new Date() + " Connection from  " + socket + ", user name \""+ userName + "\"\n");
					jtaLog.append(new Date() + " Clients = " + clients.size() + "\n");
				}		
				while(true) {
					String text = fromClient.nextLine();
					synchronized (jtaLog) {
						jtaLog.append(new Date() + " " + text + "\n");	
					}
					synchronized (clients) {
						for (NewClient newClient : clients) {
							newClient.send(text);
						}						
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {
				clients.remove(this);
				synchronized (jtaLog) {
					jtaLog.append(new Date() + " Disconnect client " + socket + ", user name \""+ userName + "\"\n");
					jtaLog.append(new Date() + " Clients = " + clients.size() + "\n");
				}
			}
		}
		
		public String getUserName() {
			return userName;
		}
		
		public void send(String text) {
			Calendar time = new GregorianCalendar();
			toClient.printf("%02d:%02d:%02d " + text + "\n", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.SECOND));
			toClient.flush();
		}
		
	}

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

}
