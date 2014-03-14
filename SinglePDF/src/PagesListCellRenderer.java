import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class PagesListCellRenderer extends JLabel implements ListCellRenderer {
	
	private static final long serialVersionUID = -1147954718288877105L;
	ImageIcon icon;
	
	public PagesListCellRenderer() {
		super();
		this.setOpaque(true);
		icon = new ImageIcon(getClass().getResource("pdficon.png"));
	}
	
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		PDFPages pages = (PDFPages)value;
		
		if (isSelected) {
            setBackground(Color.lightGray);
            //setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            //setForeground(list.getForeground());
        }
		
		this.setText(pages.toString());
		this.setIcon(icon);		
		
		return this;
	}


	@Override
	public boolean isVisible() {
		return false; // Included to remove a Java bug in Windows. For more information, see https://www.java.net//node/676933 .
		// return super.isVisible();
	}

}
