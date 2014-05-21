package net.bohush.chat;

import java.awt.BorderLayout;
import java.awt.Color;
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
	
	public UserList() {
		setLayout(new BorderLayout());
		panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		Color backgroundColor = new Color(236, 233, 216);
		panel.setBackground(backgroundColor);
		setBackground(backgroundColor);
	}
	
	public void setData(String[] stringUsers) {
		panel.removeAll();
		panel.updateUI();
		
		panel.setLayout(new GridLayout(stringUsers.length, 1, 5, 5));

		for (int i = 0; i < stringUsers.length; i++) {
			
			JPanel oneUserPanel = new JPanel(new BorderLayout(5, 5));
			
			oneUserPanel.add(new JLabel(stringUsers[i]), BorderLayout.WEST);
			oneUserPanel.add(new pmJButton(stringUsers[i]), BorderLayout.EAST);
			
			oneUserPanel.setBackground(Color.WHITE);
			panel.add(oneUserPanel);
		}
	}
	
	class pmJButton extends JButton {
		private static final long serialVersionUID = 1L;
		private String userName;
		public pmJButton(String userName) {
			super(" PM ");
			this.userName = userName;
			setContentAreaFilled(false);
			setBorder(new LineBorder(Color.BLACK));
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println(pmJButton.this.userName);
				}
			});
		}
	}

}
