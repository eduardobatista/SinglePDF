package com.singlepdf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.itextpdf.awt.geom.misc.RenderingHints;

public class PagesListCellRenderer extends JLabel implements ListCellRenderer {
	
	private static final long serialVersionUID = -1147954718288877105L;
	ImageIcon icon;
	int id = -1;
	int pagestart = 1;
	int pageend = 1;
	int totalpages = 1;
	String filename = "";
		
	public PagesListCellRenderer(Font ff) {
		super();
		this.setOpaque(true);
		icon = new ImageIcon(getClass().getResource("/pdfpage3.png"));
		this.setFont(ff);
	}
	
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		PDFPages pages = (PDFPages)value;
		id = index+1;
		pagestart = pages.getStart();
		pageend = pages.getEnd();
		totalpages = pages.getNumPages();
		filename = pages.getFileName();
		
		if (isSelected) {						
            setBackground(Color.lightGray);
        } else {
            setBackground(list.getBackground());
        }
		
		this.setMaximumSize(new Dimension(130, 167));
		this.setPreferredSize(new Dimension(130, 167));		

		return this;
	}


	@Override
	public boolean isVisible() {
		return false; // Included to remove a Java bug in Windows. For more information, see https://www.java.net//node/676933 .
		// return super.isVisible();
	}
	
	private static final int titleinset = 12;
	
	public void paintComponent(Graphics g)
    {
        //super.paintComponent(g);		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

		
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, 130, 167);
		g2.setColor(this.getForeground());
		
        g2.drawImage(icon.getImage(), 0, 0, null);
				
		Font f = this.getFont().deriveFont(Font.BOLD,11f);
		g2.setFont(f);
		FontMetrics fm = g2.getFontMetrics(f);
		
		g2.setColor(Color.lightGray);
		
		String idtext = Integer.toString(id);		
		Rectangle2D r2 = fm.getStringBounds(idtext,g2);
		g2.drawString(idtext, 120-(int)r2.getWidth(), 20);
		
		String pagestext = "Pgs. " + Integer.toString(pagestart) + " to " + Integer.toString(pageend) + "  [" + Integer.toString(totalpages) + "]";
		r2 = fm.getStringBounds(pagestext,g2);		
		g2.drawString(pagestext, 65-(int)(r2.getWidth()/2), 154);
		
		g2.setColor(this.getForeground());
		
		g2.setFont(f.deriveFont(12f));
		fm = g2.getFontMetrics();
		int maxsize = 130 - 2*titleinset;		
		r2 = fm.getStringBounds(filename,g2);
		if (r2.getWidth() < maxsize) {
			g2.drawString(filename, 65-(int)(r2.getWidth()/2), 85);
		} else {
			int ct = (filename.length() < 20) ? filename.length() : 20;
			do {
				ct--;				
				r2 = fm.getStringBounds(filename.substring(0,ct) + "...",g2);
			} while (r2.getWidth() > maxsize);
			g2.drawString(filename.substring(0,ct) + "...", 65-(int)(r2.getWidth()/2), 85);
		}
		
    }

}
