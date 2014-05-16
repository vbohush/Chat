package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTextArea jta = new JTextArea();
	private PrintWriter toServer;
	private JTextField jtfMessage = new JTextField();
	
	private Socket socket;
	private String userName;
	
	public Client(Socket socket, String userName) {
		this.socket = socket;
		this.userName = userName;
		setLayout(new BorderLayout(5, 5));
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		jta.setWrapStyleWord(true);
	    jta.setLineWrap(true);
	    jta.setEditable(false);
	    jta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
	    JScrollPane jsp = new JScrollPane(jta);

		jsp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
			}
		});
	    
		
	    mainPanel.add(jsp, BorderLayout.CENTER);
	    
	    JPanel jpMessage = new JPanel(new BorderLayout(5, 5));
	    jpMessage.add(new JLabel("Enter text "), BorderLayout.WEST);
	    jpMessage.add(jtfMessage, BorderLayout.CENTER);
	    mainPanel.add(jpMessage, BorderLayout.SOUTH);
	    add(mainPanel, BorderLayout.CENTER);
	    
	    jtfMessage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!jtfMessage.getText().equals("")) {
					toServer.println(Client.this.userName + ": " + jtfMessage.getText());
					toServer.flush();
					jtfMessage.setText("");
				}
			}
		});
		
		try {
			toServer = new PrintWriter(socket.getOutputStream());
			new ReceiveMessage(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ReceiveMessage implements Runnable {
		private Socket socket;
		public ReceiveMessage(Socket socket) {
			this.socket = socket;
			Thread thread = new Thread(this);
			thread.start();
		}
		public void run() {
			try {
				@SuppressWarnings("resource")
				Scanner fromServer = new Scanner(socket.getInputStream());
				while(true) {
					String text = fromServer.nextLine();
					jta.append(text + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException e) {				
			}
		}
	}

}
