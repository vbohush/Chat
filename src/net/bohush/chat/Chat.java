package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Chat extends JPanel{
	private static final long serialVersionUID = 1L;
	
	public Chat() {
		setLayout(new BorderLayout());
		JPanel jpStart = new JPanel(new GridLayout(1, 2, 5, 5));
		jpStart.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JPanel jpServer = new JPanel(new BorderLayout());
		jpServer.setBorder(new TitledBorder("Server"));
		
		JPanel jpConfigServer = new JPanel(new GridLayout(3, 3, 5, 20));
		jpConfigServer.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel jlblServerIp = new JLabel("Ip: ");
		jlblServerIp.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigServer.add(jlblServerIp);
		JTextField jtfServerIp = new JTextField(7);
		jpConfigServer.add(jtfServerIp);
		
		JLabel jlblServerPort = new JLabel("Port: ");
		jlblServerPort.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigServer.add(jlblServerPort);
		JTextField jtfServerPort = new JTextField(7);
		jpConfigServer.add(jtfServerPort);
		
		JLabel jlblServerCount = new JLabel("Max Users Count: ");
		jlblServerCount.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigServer.add(jlblServerCount);
		JTextField jtfServerCount = new JTextField(7);
		jpConfigServer.add(jtfServerCount);
		
		JPanel jpStartServer = new JPanel(new BorderLayout());
		jpStartServer.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton jbtnStartServer = new JButton("Start Server");
		jpStartServer.add(jbtnStartServer, BorderLayout.CENTER);		
		
		jpServer.add(jpConfigServer, BorderLayout.CENTER);
		jpServer.add(jpStartServer, BorderLayout.SOUTH);
		
		jpStart.add(jpServer);

		JPanel jpClient = new JPanel(new BorderLayout());
		jpClient.setBorder(new TitledBorder("Client"));
			
		JPanel jpConfigClient = new JPanel(new GridLayout(3, 3, 5, 20));
		jpConfigClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel jlblConnectToIp = new JLabel("Ip: ");
		jlblConnectToIp.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblConnectToIp);
		JTextField jtfConnectToIp = new JTextField(7);
		jpConfigClient.add(jtfConnectToIp);
		
		JLabel jlblConnectToPort = new JLabel("Port: ");
		jlblConnectToPort.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblConnectToPort);
		JTextField jtfConnectToPort = new JTextField(7);
		jpConfigClient.add(jtfConnectToPort);
		
		JLabel jlblUserName = new JLabel("User Name: ");
		jlblUserName.setHorizontalAlignment(SwingConstants.RIGHT);
		jpConfigClient.add(jlblUserName);
		JTextField jtfUserName = new JTextField(7);
		jpConfigClient.add(jtfUserName);
		
		
		
		
		JPanel jpStartClient = new JPanel(new BorderLayout());
		jpStartClient.setBorder(new EmptyBorder(10, 10, 10, 10));
		JButton jbtnStartClient = new JButton("Connect to Server");
		jpStartClient.add(jbtnStartClient, BorderLayout.CENTER);
		
		
		jpClient.add(jpConfigClient, BorderLayout.CENTER);
		jpClient.add(jpStartClient, BorderLayout.SOUTH);
		
		jpStart.add(jpClient);
		
		add(jpStart, BorderLayout.CENTER);
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Chat, created by Viktor Bohush");
		frame.add(new Chat());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setMinimumSize(new Dimension(frame.getWidth(), frame.getHeight()));
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
