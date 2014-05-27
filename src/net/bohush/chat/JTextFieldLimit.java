package net.bohush.chat;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JTextFieldLimit extends JTextField {
	private static final long serialVersionUID = 1L;
	private int limit;

	
	
	public JTextFieldLimit(String text, int columns, int limit) {
		super(columns);
		this.limit = limit;
		this.setText(text);
	}

	public JTextFieldLimit(int limit) {
		super();
		this.limit = limit;
	}

	@Override
	protected Document createDefaultModel() {
		return new LimitDocument();
	}

	private class LimitDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null) {
				return;
			}
			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}

	}

}
