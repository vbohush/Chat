package net.bohush.chat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JPanel;

public 	class ColorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Color color;

	public ColorPanel(Color color) {
		this.color = color;
		addMouseListener(new MouseAdapter() {				
			@Override
			public void mouseReleased(MouseEvent e) {
				Color selectedColor = JColorChooser.showDialog(null, "Pick a Color", ColorPanel.this.color);
				if(selectedColor != null) {
					ColorPanel.this.color = selectedColor;
					repaint();
				}
			}				
		});
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(18, 18);
	}
	
	public void setColor(Color color) {
		this.color = color;
		repaint();
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}
