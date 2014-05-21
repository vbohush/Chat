package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class UserList extends JPanel{
	private static final long serialVersionUID = 1L;
	private JPanel panel;
	private Client clientPanel;
	
	public UserList(Client clientPanel) {
		this.clientPanel = clientPanel;
		setLayout(new BorderLayout());
		panel = new JPanel();
		add(panel, BorderLayout.NORTH);
	}
	
	public void setData(String[] stringUsers) {
		panel.removeAll();
		panel.setLayout(new GridLayout(stringUsers.length, 1, 5, 5));

		for (int i = 0; i < stringUsers.length; i++) {
			
			JPanel oneUserPanel = new JPanel(new BorderLayout(5, 5));
			
			JLabel userNameLabel = new JLabel(stringUsers[i]);
			userNameLabel.setFont(userNameLabel.getFont().deriveFont(Font.BOLD));
			oneUserPanel.add(userNameLabel, BorderLayout.WEST);
			oneUserPanel.add(new PmJButton(stringUsers[i], clientPanel), BorderLayout.EAST);
			
			oneUserPanel.setBackground(Color.WHITE);
			panel.add(oneUserPanel);
		}
		
		panel.updateUI();
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
