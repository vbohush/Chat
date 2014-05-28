package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class UserList extends JPanel{
	private static final long serialVersionUID = 1L;
	private JPanel panel;
	private Client clientPanel;
	private boolean isAdmin;
	
	
	public UserList(Client clientPanel, boolean isAdmin) {
		this.isAdmin = isAdmin;
		this.clientPanel = clientPanel;
		setLayout(new BorderLayout());
		panel = new JPanel();
		add(panel, BorderLayout.NORTH);
	}
	
	public void setData(String userName, String[] stringUsers, String[] ips) {
		panel.removeAll();
		panel.setLayout(new GridLayout(stringUsers.length, 1, 5, 5));

		for (int i = 0; i < stringUsers.length; i++) {
			
			JPanel oneUserPanel = new JPanel(new BorderLayout(5, 5));
			
			JLabel userNameLabel = new JLabel(stringUsers[i]);
			userNameLabel.setFont(userNameLabel.getFont().deriveFont(Font.BOLD));
			oneUserPanel.add(userNameLabel, BorderLayout.WEST);
			if(!userName.equals(stringUsers[i])){
				if(isAdmin) {
					JPanel userButtonsPanel = new JPanel(new GridLayout(1, 2, 2, 0));
					userButtonsPanel.setBackground(Color.WHITE);
					userButtonsPanel.add(new BanButton(stringUsers[i], ips[i], clientPanel));
					userButtonsPanel.add(new PmJButton(stringUsers[i], clientPanel));
					oneUserPanel.add(userButtonsPanel, BorderLayout.EAST);
				} else {
					oneUserPanel.add(new PmJButton(stringUsers[i], clientPanel), BorderLayout.EAST);
				}		
			}
			oneUserPanel.setBackground(Color.WHITE);
			panel.add(oneUserPanel);
		}
		
		panel.updateUI();
	}
	
	class BanButton extends JButton {
		private static final long serialVersionUID = 1L;
		private String userName;
		private Client clientPanel;
		private String ip;
		
		public BanButton(String userName, String ip, Client clientPanel) {
			super("Ban");
			this.ip = ip;
			this.clientPanel = clientPanel;
			this.userName = userName;
			setContentAreaFilled(false);
			setBorder(new LineBorder(Color.BLACK));
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int confirm = JOptionPane.showOptionDialog( null, "Are you sure you want to ban ip \"" + BanButton.this.ip + "\" of user \"" + BanButton.this.userName + "\"",
					"Ban confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
					if (confirm == JOptionPane.YES_OPTION) {
						BanButton.this.clientPanel.banUser(BanButton.this.ip);
					}
					BanButton.this.clientPanel.requestFocus();
				}
			});
		}
	} 
	
	class PmJButton extends JButton {
		private static final long serialVersionUID = 1L;
		private String userName;
		private Client clientPanel;
		
		public PmJButton(String userName, Client clientPanel) {
			super(" PM ");
			this.clientPanel = clientPanel;
			this.userName = userName;
			setContentAreaFilled(false);
			setBorder(new LineBorder(Color.BLACK));
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					PmJButton.this.clientPanel.setPmUser(PmJButton.this.userName);
				}
			});
		}
	}

}
