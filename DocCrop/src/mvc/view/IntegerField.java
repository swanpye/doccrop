package mvc.view;

/*
 * Copyright (C) 2001-2003 Colin Bell
 * colbell@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * This class is a <CODE>TextField</CODE> that only allows integer values to be
 * entered into it.
 * 
 * @author <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>, Tomas Toss
 */	
public class IntegerField extends JTextField {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public IntegerField() {
		super();
	}

	/**
	 * Constructor specifying the field width.
	 * 
	 * @param cols
	 *            Number of columns.
	 */
	public IntegerField(int cols) {
		super(cols);
	}

	/**
	 * Retrieve the contents of this field as an <code>int</code>.
	 * 
	 * @return the contents of this field as an <code>int</code>.
	 */
	public int getInt() {
		final String text = getText();
		if (text == null || text.length() == 0) {
			return 0;
		}
		return Integer.parseInt(text);
	}

	/**
	 * Set the contents of this field to the passed <code>int</code>.
	 * 
	 * @param value
	 *            The new value for this field.
	 */
	public void setInt(int value) {
		setText(String.valueOf(value));
	}

	/**
	 * Create a new document model for this control that only accepts integral
	 * values.
	 * 
	 * @return The new document model.
	 */
	protected Document createDefaultModel() {
		return new IntegerDocument();
	}

	/**
	 * This document only allows integral values to be added to it.
	 */
	static class IntegerDocument extends PlainDocument {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
			if (str != null) {
				try {
					if (offs == 0 && str.equals("-")
							&& !getText(0, 1).equals("-"))
						if (getLength() + str.length() > 3) {
						} else {
							super.insertString(offs, str, a);
						}
					else {
						Integer.decode(str);
						if (getLength() + str.length() > 2) {
						} else {
							super.insertString(offs, str, a);
						}
					}
				} catch (NumberFormatException ex) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		}
	}
}
