package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.PrintWriter;
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
	private JTextField jtfMessage = new JTextField();
	
	private PrintWriter toServer;
	private Scanner fromServer;
	private String userName;
	
	public Client(PrintWriter toServer, Scanner fromServer, String userName) {
		this.userName = userName;
		this.toServer = toServer;
		this.fromServer = fromServer;
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
	    
		new ReceiveMessage();
		jtfMessage.requestFocus();
		
	    jtfMessage.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!jtfMessage.getText().equals("")) {
					Client.this.toServer.println(Client.this.userName + ": " + jtfMessage.getText());
					Client.this.toServer.flush();
					jtfMessage.setText("");
					jtfMessage.requestFocus();
				}
			}
		});
	    
	}
	
	public void setFocus() {
		jtfMessage.requestFocus();
	}
		
	class ReceiveMessage implements Runnable {
		public ReceiveMessage() {
			Thread thread = new Thread(this);
			thread.start();
		}
		public void run() {
			try {
				while(true) {
					String text = fromServer.nextLine();
					jta.append(text + "\n");
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}
		}
	}

}
