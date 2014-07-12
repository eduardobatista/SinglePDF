package com.singlepdf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import com.jeta.forms.components.panel.FormPanel;


public class PagesListCellEditor extends JPanel implements TableCellEditor, ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FormPanel editorPanel;
	private PDFPages saveditem;
	private JComboBox comboStart;
	private JComboBox comboEnd;
	
	public PagesListCellEditor() {
		
		super();
		this.setBackground(Color.white);
		this.setEnabled(true);
		
		editorPanel = new FormPanel("editoritem.jfrm");
		comboStart = editorPanel.getComboBox("comboStart");
		comboEnd = editorPanel.getComboBox("comboEnd");
		comboStart.addItemListener(this);
		comboEnd.addItemListener(this);
		
		this.setLayout(new BorderLayout());
		this.add(editorPanel, BorderLayout.CENTER);
		
	}

	public void addCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub

	}

	public void cancelCellEditing() {
		// TODO Auto-generated method stub

	}

	public Object getCellEditorValue() {		
		return saveditem;
	}

	public boolean isCellEditable(EventObject arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof MouseEvent) {
			if ( ((MouseEvent)arg0).getClickCount() > 1 ) { return true; }
			else { return false; }
		}
		return false;
	}

	public void removeCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub
	}

	public boolean shouldSelectCell(EventObject arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean stopCellEditing() {
		saveditem.setRange(Integer.parseInt(comboStart.getSelectedItem().toString()), Integer.parseInt(comboEnd.getSelectedItem().toString()));
		System.out.println("ok");
		return true;
	}

	public Component getTableCellEditorComponent(JTable arg0, Object arg1,
			boolean arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		saveditem = (PDFPages)arg1;
		comboStart.removeAllItems();
		comboEnd.removeAllItems();
		for (int i = 0; i < 10; i++) {
			comboStart.addItem(Integer.toString(i+1));
			comboEnd.addItem(Integer.toString(i+1));
		}
		comboStart.setSelectedIndex(0);
		comboEnd.setSelectedIndex(9);
		editorPanel.getLabel("fileNameLabel").setText(saveditem.getFileName());		
		return this;		
	}

	public void itemStateChanged(ItemEvent e) {	
		if (comboStart.getItemCount() == 0) { return; }
		if (comboEnd.getItemCount() == 0) { return; }
		if (e.getSource() == comboStart) {
			if (comboStart.getSelectedIndex() > comboEnd.getSelectedIndex()) {
				comboEnd.setSelectedIndex(comboStart.getSelectedIndex());
			}
		} else if (e.getSource() == comboEnd) {
			if (comboEnd.getSelectedIndex() < comboStart.getSelectedIndex()) {
				comboStart.setSelectedIndex(comboEnd.getSelectedIndex());
			}
		}		
	}
	
	
	

}
