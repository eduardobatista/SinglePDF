package com.singlepdf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.jeta.forms.components.panel.FormPanel;

public class SinglePDF implements ListSelectionListener, ItemListener, ActionListener {
	
	private static final String LAST_DIR = "last_dir";
	private static final String LAST_DIR_PGLIST = "last_dir_pglist";
	
	private JFrame frame;
	private FormPanel mainFormPanel;
	private JList mainList;
	
	private JComboBox comboStart;
	private JComboBox comboEnd;
	
	private JButton bRemove;
	private JButton bDuplicate;
	private JButton bUp;
	private JButton bDown;
	private JButton bSplit;
	private JButton bBuild;
	private JButton bSinglePages;
	private JCheckBox checkNoCopyPaste;
	
	private JLabel statusbar;
	
	private int counter = 0;
	
	ArrayList<PDFPages> pagesList = new ArrayList<PDFPages>();
	
	private ImageIcon smallPdfIcon = new ImageIcon(getClass().getResource("/pdficonsmall.png"));;
	
	public SinglePDF() {
		
		try { UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel"); } 
		catch (Exception e) { e.printStackTrace(); }		
		frame = new JFrame("SinglePDF 0.21b");
				
		mainFormPanel = new FormPanel("mainwindow.jfrm");	
		mainList = mainFormPanel.getList("mainList");		
		mainList.setDragEnabled(true);
		mainList.setDropMode(DropMode.INSERT);		
		mainList.setTransferHandler(thandler);
		mainList.setSelectionBackground(Color.white);
		mainList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mainList.addListSelectionListener(this);
		mainList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		mainList.setVisibleRowCount(-1);
		
		comboStart = mainFormPanel.getComboBox("comboStart");		
		comboEnd = mainFormPanel.getComboBox("comboEnd");
		for (int i = 1; i < 11; i++) {
			comboStart.addItem(Integer.toString(i));
			comboEnd.addItem(Integer.toString(i));
		}
		comboStart.addItemListener(this);
		comboEnd.addItemListener(this);
		
		bRemove = (JButton)mainFormPanel.getButton("bRemove");
		bRemove.addActionListener(this);
		bDuplicate = (JButton)mainFormPanel.getButton("bDuplicate");
		bDuplicate.addActionListener(this);
		bUp = (JButton)mainFormPanel.getButton("bUp");
		bUp.addActionListener(this);
		bDown = (JButton)mainFormPanel.getButton("bDown");
		bDown.addActionListener(this);
		bSplit = (JButton)mainFormPanel.getButton("bSplit");
		bSplit.addActionListener(this);
		bBuild = (JButton)mainFormPanel.getButton("bBuild");
		bBuild.addActionListener(this);
		bSinglePages = (JButton)mainFormPanel.getButton("bSinglePages");
		bSinglePages.addActionListener(this);
		
		checkNoCopyPaste = mainFormPanel.getCheckBox("checkNoCopyPaste");
		
		statusbar = mainFormPanel.getLabel("statusBar");
		
		this.setupKeyBindings();
		
		this.createMenuBar();
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(mainFormPanel, BorderLayout.CENTER);
		frame.setIconImage(new ImageIcon(getClass().getResource("/pdfmultiicon.png")).getImage());
		frame.pack();		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 700;
		int height = 600;
		frame.setSize(width,height);
		frame.setLocation((screenSize.width-width)/2, (screenSize.height-height)/2);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mainList.setCellRenderer(new PagesListCellRenderer(bBuild.getFont()));
						
	}
	
	JMenuItem menuOpen;
	JMenuItem menuSave;
	JMenuItem menuSaveAs;
	JMenuItem menuExit;
	JMenuItem menuAbout;
	public void createMenuBar() {
		
		JMenuBar menubar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menubar.add(fileMenu);
		
		menuOpen = new JMenuItem("Open page list",KeyEvent.VK_O);
		fileMenu.add(menuOpen);
		menuOpen.addActionListener(this);
		menuSave = new JMenuItem("Save page list",KeyEvent.VK_S);
		fileMenu.add(menuSave);
		menuSave.addActionListener(this);
		menuSaveAs = new JMenuItem("Save page list as...",KeyEvent.VK_A);
		fileMenu.add(menuSaveAs);
		menuSaveAs.addActionListener(this);
		fileMenu.addSeparator();
		menuExit = new JMenuItem("Exit",KeyEvent.VK_X);
		fileMenu.add(menuExit);
		menuExit.addActionListener(this);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menubar.add(helpMenu);
		menuAbout = new JMenuItem("About",KeyEvent.VK_A);
		helpMenu.add(menuAbout);
		menuAbout.addActionListener(this);
		
		frame.setJMenuBar(menubar);
		
	}
	
	public void setupKeyBindings() {
		String delAction = "deleteItems";
        KeyStroke delKey = KeyStroke.getKeyStroke("DELETE");
        mainList.getInputMap().put(delKey, delAction);
        mainList.getActionMap().put(delAction, new AbstractAction() {
			private static final long serialVersionUID = 8386085749787167324L;
			public void actionPerformed(ActionEvent e) {
	             removeSelectedItens();
			}
		});   
        String moveUpAction = "moveItemUp";
        KeyStroke ctrlplusupKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.CTRL_DOWN_MASK);
        mainList.getInputMap().put(ctrlplusupKey, moveUpAction);
        mainList.getActionMap().put(moveUpAction, new AbstractAction() {
			private static final long serialVersionUID = 8386085749787167324L;
			public void actionPerformed(ActionEvent e) {
	             moveItensUp();
			}
		});
        String moveDownAction = "moveItemDown";
        KeyStroke ctrlplusdownKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.CTRL_DOWN_MASK);
        mainList.getInputMap().put(ctrlplusdownKey, moveDownAction);
        mainList.getActionMap().put(moveDownAction, new AbstractAction() {
			private static final long serialVersionUID = 8386085749787167324L;
			public void actionPerformed(ActionEvent e) {
	             moveItensDown();
			}
		});
	}
	
	/**
	 *  This code was obtained in:
	 *  http://stackoverflow.com/questions/9192371/dragn-drop-files-from-the-os-to-java-application-swin  
	 */
	TransferHandler thandler = new TransferHandler() {		
		
		private static final long serialVersionUID = 8794701420169765183L;		
		private final DataFlavor pdfpagesFlavor = new ActivationDataFlavor(PDFPages.class, DataFlavor.javaJVMLocalObjectMimeType, "PDFPages");
		private final DataFlavor pdfpagelistFlavor = new ActivationDataFlavor(PDFPages[].class, DataFlavor.javaJVMLocalObjectMimeType, "PDFPagesVector");
		private PDFPages pgs = null;
		private PDFPages[] pglist = null;
				
		@Override
        public boolean canImport(TransferHandler.TransferSupport info) {  
			if (!info.isDrop()) {
				return false;		        
		    }
            if (!info.isDataFlavorSupported(pdfpagesFlavor) && !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && !info.isDataFlavorSupported(pdfpagelistFlavor) ) {
                return false;
            }
            return true;
        }
		
		int[] indexOfExported = new int[]{-1};
		int indexOfImported = -1;
		@Override
		protected Transferable createTransferable(JComponent c) {
			JList mlist = (JList)c;			
			int[] idcs = mlist.getSelectedIndices();
			if (idcs.length == 1) {
				pgs = (PDFPages)mlist.getSelectedValue();
				PDFPages pgstoexport = null;
				try {
					pgstoexport = new PDFPages(0, pgs.getFile(), pgs.getStart(), pgs.getEnd());
				} catch (IOException e) {					
					e.printStackTrace();
				}
				indexOfExported = idcs;
				return new DataHandler(pgstoexport, pdfpagesFlavor.getMimeType());
			} else {
				pglist = new PDFPages[idcs.length];
				PDFPages temppages;
				for (int i = 0; i < idcs.length; i++) {
					temppages = (PDFPages)mlist.getModel().getElementAt(idcs[i]);
					try {
						pglist[i] = new PDFPages(0,temppages.getFile(),temppages.getStart(),temppages.getEnd());
					} catch (IOException e) {
						pglist[i] = null;
						e.printStackTrace();
					}
//					pglist[i] = (PDFPages)mlist.getModel().getElementAt(idcs[i]);
				}
				indexOfExported = idcs;
				return new DataHandler(pglist, pdfpagelistFlavor.getMimeType());
			}
		}
		
		@Override
		protected void exportDone(JComponent source, Transferable data,	int action) {
			
			JList list = (JList)source;
			int[] indices = new int[indexOfExported.length];
			for (int i = 0; i < indices.length; i++) { indices[i] = indexOfImported+i; }			
			list.setSelectedIndices(indices);
			
			if (action == TransferHandler.MOVE) {
				DefaultListModel model = ((DefaultListModel)mainList.getModel());
				for (int i = indexOfExported.length-1; i >= 0 ; i--) {
					if (indexOfExported[i] < indexOfImported) {
						model.remove(indexOfExported[i]);												
					} else {
						model.remove(indexOfExported[i]+indexOfExported.length);
					}
				}
			}
			
		}

		@Override
		public int getSourceActions(JComponent arg0) {
			return TransferHandler.COPY_OR_MOVE;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport info) {
			if (!info.isDrop()) {
	            return false;
	        }
			
			if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				Transferable t = info.getTransferable();
				List<File> data;
				try { data = ((List<File>)t.getTransferData(DataFlavor.javaFileListFlavor));  } 
				catch (Exception e) { return false; }
				DefaultListModel model = (DefaultListModel) mainList.getModel();			 
				for (int i = 0; i < data.size(); i++) {
					PDFPages aux;
					try {
						aux = new PDFPages(counter, data.get(i));
						pagesList.add(aux);
						model.addElement(aux);
						counter++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return true;
			} else if (info.isDataFlavorSupported(pdfpagesFlavor)) {
				try {
					PDFPages pages = (PDFPages)info.getTransferable().getTransferData(pdfpagesFlavor);
					JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
					DefaultListModel model = (DefaultListModel)mainList.getModel();
					indexOfImported = dl.getIndex();
					model.add(dl.getIndex(), pages);
					mainList.setSelectedIndex(dl.getIndex());
				} catch (UnsupportedFlavorException e) {
					setStatus("Flavor issue when dropping...");
					e.printStackTrace();
				} catch (IOException e) {
					setStatus(e.getMessage());
					e.printStackTrace();
				}				
				return true;
			} else if (info.isDataFlavorSupported(pdfpagelistFlavor)) {				
				try {
					PDFPages[] pagelist = (PDFPages[])info.getTransferable().getTransferData(pdfpagelistFlavor);
					JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
					DefaultListModel model = (DefaultListModel)mainList.getModel();
					indexOfImported = dl.getIndex();
					int idx = dl.getIndex();
					for (int i = 0; i < pagelist.length; i++) {
						model.add(idx++, pagelist[i]);
					}
					mainList.setSelectedIndex(dl.getIndex());
				} catch (UnsupportedFlavorException e) {
					setStatus("Flavor issue when dropping...");
					e.printStackTrace();
				} catch (IOException e) {
					setStatus(e.getMessage());
					e.printStackTrace();
				}		
				return true;
			} else {
				setStatus("Unsupported data dropped on the list...");
				return false;
			}

		 }
			
	};
	
	public void setStatus(String msg) {
		if (msg == null) {
			statusbar.setText(" ");
		} else {
			statusbar.setText(" " + msg);
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new SinglePDF();		
	}
	
	private static final int OUTPUTDIR = 0;
	private static final int PGLISTDIR = 1;
	
	private File getLastDir(int type) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		if (type == OUTPUTDIR) {
			return new File(prefs.get(LAST_DIR,""));
		} else {
			return new File(prefs.get(LAST_DIR_PGLIST,""));
		}		
	}
	
	private void saveLastDir(File dir, int type) {
		try {
			Preferences prefs = Preferences.userNodeForPackage(this.getClass());
			if (type == OUTPUTDIR) {
				prefs.put(LAST_DIR,dir.getCanonicalPath());
			} else {
				prefs.put(LAST_DIR_PGLIST,dir.getCanonicalPath());
			}
			
		} catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	
	public void openListFile() throws FileNotFoundException, IOException {
		
		// TODO: Check if list is saved
		
		final JFileChooser fc = new JFileChooser(getLastDir(PGLISTDIR));
//		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		File input = new File("input.pglist");
		
		FileFilter filter = new FileFilter() {			
			@Override
			public String getDescription() {
				return "Page list files";
			}			
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) { return true; }
				String aux = f.getName().toLowerCase();
				if (aux.endsWith("pglst")) { return true; }
				else { return false; }
			}
		};
		fc.addChoosableFileFilter(filter);
		
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			this.setStatus("Operation cancelled.");	
			return; 
		} else if (returnVal == JFileChooser.ERROR_OPTION) {
			this.setStatus("Error choosing file...");	
			return; 
		} else if (returnVal == JFileChooser.APPROVE_OPTION) {
			input = fc.getSelectedFile();
			//if (!output.getName().endsWith("pglst")) { output = new File(output.getAbsoluteFile() + ".pglst"); }
			if (!input.exists()) {
				this.setStatus("Invalid file.");
				return;
			} else {

				BufferedReader in = new BufferedReader(new FileReader(input));
				File basedir = input.getParentFile();
				DefaultListModel model = (DefaultListModel)mainList.getModel();
				model.clear();
				int id = 0;
				String aux; 
				while ( (aux = in.readLine()) != null ) {
					int ct1 = aux.indexOf(";", 0);
					File actualFile = null;
					if (aux.substring(0, ct1).startsWith("file:")) {
						try {
							actualFile = new File(new URI(aux.substring(0, ct1)));
						} catch (URISyntaxException e) {
							actualFile = null;
							e.printStackTrace();
						}
					} else {
						actualFile = new File(basedir,aux.substring(0, ct1));
					}
					int ct2 = aux.indexOf(";", ct1+1);
					int pstart = Integer.parseInt(aux.substring(ct1+1,ct2));
					ct1 =  aux.indexOf(";", ct2+1);
					int pend = Integer.parseInt(aux.substring(ct2+1,ct1));
					try {
						model.addElement(new PDFPages(id++, actualFile, pstart, pend));
					} catch (Exception e) {
						this.setStatus("Some files from the list are missing or invalid...");
						e.printStackTrace();
					}
				}
				
				in.close();
				saveLastDir(basedir,PGLISTDIR);
								
			}		
		}
		
		
	}
	
	public void saveListFile(boolean selectFile) throws IOException {
		
		DefaultListModel model = (DefaultListModel)mainList.getModel();
		if (model.size() == 0) {			
			this.setStatus("Empty list.");
			return;
		}
		
		File output = new File("output.pglst");
				
		final JFileChooser fc = new JFileChooser(this.getLastDir(PGLISTDIR));
//		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		FileFilter filter = new FileFilter() {			
			@Override
			public String getDescription() {
				return "Page list files";
			}			
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) { return true; }
				String aux = f.getName().toLowerCase();
				if (aux.endsWith("pglst")) { return true; }
				else { return false; }
			}
		};
		fc.addChoosableFileFilter(filter);
		
		int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			this.setStatus("Operation cancelled.");	
			return; 
		} else if (returnVal == JFileChooser.ERROR_OPTION) {
			this.setStatus("Error choosing file...");	
			return; 
		} else if (returnVal == JFileChooser.APPROVE_OPTION) {
			output = fc.getSelectedFile();
			if (!output.getName().endsWith("pglst")) { output = new File(output.getAbsoluteFile() + ".pglst"); }
			if (output.exists()) {
				String[] options = new String[]{"Yes","No"};
				final JOptionPane optionPane = new JOptionPane(
					    "The select file already exists,\n" +
					    "do you want to overwrite it?",
					    JOptionPane.QUESTION_MESSAGE,
					    JOptionPane.YES_NO_OPTION,
					    null,options,options[1]);
				if (optionPane.getValue().equals("No")) {
					this.setStatus("Operation cancelled.");					
					return;
				} else {
					output.delete();
					output.createNewFile();
				}
			} else {
				output.createNewFile();
			}		
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		File basedir = output.getParentFile();
		for (int i = 0; i < model.getSize(); i++) {
			PDFPages pages = (PDFPages)model.get(i);
			File file = pages.getFile();
			String relative = basedir.toURI().relativize(file.toURI()).toString();
			System.out.println(relative);
			out.write(relative + ";" + Integer.toString(pages.getStart()) + ";" + Integer.toString(pages.getEnd()) + ";");
			out.newLine();
		}
		
		out.close();
		
		saveLastDir(basedir,PGLISTDIR);
		
	}
	
	public void generateOutputFile() throws IOException, DocumentException {
		
		DefaultListModel model = (DefaultListModel)mainList.getModel();
		if (model.size() == 0) {			
			this.setStatus("Empty list.");
			return;
		}
		
		File output = new File("output.pdf");
				
		final JFileChooser fc = new JFileChooser(getLastDir(OUTPUTDIR));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		FileFilter filter = new FileFilter() {			
			@Override
			public String getDescription() {
				return "PDF files";
			}			
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) { return false; }
				String aux = f.getName().toLowerCase();
				if (aux.endsWith("pdf")) { return true; }
				else { return false; }
			}
		};	
		FileView fview = new FileView() {

			@Override
			public Icon getIcon(File f) {
				String aux = f.getName().toLowerCase();
				if (aux.endsWith("pdf")) {
					return smallPdfIcon;
				}
				return super.getIcon(f);
			}
			
		};
		fc.addChoosableFileFilter(filter);
		fc.setFileView(fview);
		
		int returnVal = fc.showSaveDialog(frame);
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			this.setStatus("Operation cancelled.");	
			return; 
		} else if (returnVal == JFileChooser.ERROR_OPTION) {
			this.setStatus("Error choosing file...");	
			return; 
		} else if (returnVal == JFileChooser.APPROVE_OPTION) {
			output = fc.getSelectedFile();
			if (!output.getName().endsWith("pdf")) { output = new File(output.getAbsoluteFile() + ".pdf"); }
			if (output.exists()) {
				String[] options = new String[]{"Yes","No"};
				final JOptionPane optionPane = new JOptionPane(
					    "The select file already exists,\n" +
					    "do you want to overwrite it?",
					    JOptionPane.QUESTION_MESSAGE,
					    JOptionPane.YES_NO_OPTION,
					    null,options,options[1]);
				if (optionPane.getValue().equals("No")) {
					this.setStatus("Operation cancelled.");					
					return;
				} else {
					output.delete();
					output.createNewFile();
				}				
			} else {
				output.createNewFile();
			}		
		}
				
		Document document = new Document();
		PdfCopy copy = new PdfCopy(document, new FileOutputStream(output));
		if (checkNoCopyPaste.isSelected()) {
			copy.setEncryption(new byte[]{0x30}, new byte[]{}, 0, PdfCopy.AllowCopy);
		}		
		document.open();
		
		PdfReader reader;
		PDFPages pages;
		
		for (int i = 0; i < model.size(); i++) {
			pages = (PDFPages)model.get(i);
			reader = new PdfReader(pages.getAbsolutePath());
			for (int j = pages.getStart(); j <= pages.getEnd(); j++) {
				copy.addPage(copy.getImportedPage(reader,j));				
			}
			copy.freeReader(reader);
			reader.close();
		}
		document.close();
		
		setStatus("The file " + output.getName() + " was successfully generated.");
		
		saveLastDir(output.getParentFile(),OUTPUTDIR);
		
	}

	boolean populating = false;
	public void valueChanged(ListSelectionEvent e) {
		int[] idcs = mainList.getSelectedIndices();
		if (idcs.length == 1) {
			PDFPages pages = ((PDFPages)mainList.getSelectedValue());
			populating = true;
			comboStart.setEnabled(true);
			comboEnd.setEnabled(true);
			bSplit.setEnabled(true);
			comboStart.removeAllItems();
			comboEnd.removeAllItems();
			for (int i = 1; i <= pages.getNumPages(); i++) {
				comboStart.addItem(Integer.toString(i));
				comboEnd.addItem(Integer.toString(i));
			}
			comboStart.setSelectedIndex(pages.getStart()-1);
			comboEnd.setSelectedIndex(pages.getEnd()-1);
			populating = false;			
		} else if (idcs.length > 1) {
			comboStart.setEnabled(false);
			comboEnd.setEnabled(false);
			bSplit.setEnabled(false);
		}		
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (populating) { return; }
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
		if (mainList.getSelectedIndex() >= 0) {
			PDFPages pages = ((PDFPages)mainList.getSelectedValue());
			pages.setRange(comboStart.getSelectedIndex()+1, comboEnd.getSelectedIndex()+1);
			mainList.repaint();
		}
	}
	
	public void removeSelectedItens() {
		int[] idx = mainList.getSelectedIndices();		
		if (idx.length > 0) {
			DefaultListModel model = ((DefaultListModel)mainList.getModel());
			for (int i = idx.length-1; i >= 0; i--) {
				model.remove(idx[i]);
			}
			mainList.setSelectedIndex(-1);
		}				
	}
	
	public void moveItensUp() {
		int[] idcs = mainList.getSelectedIndices();
		if (idcs.length == 0) { return; }
		else if (idcs[0] > 0) {
			DefaultListModel model = ((DefaultListModel)mainList.getModel());
			for (int i = 0; i < idcs.length; i++) {
				Object obj = model.remove(idcs[i]);
				idcs[i] = idcs[i] - 1;
				model.insertElementAt(obj, idcs[i]);
			}
			mainList.setSelectedIndices(idcs);
		}
	}
	
	public void moveItensDown() {
		int[] idcs = mainList.getSelectedIndices();
		DefaultListModel model = ((DefaultListModel)mainList.getModel());
		if (idcs.length == 0) { return; }
		else if (idcs[idcs.length-1] < (model.getSize()-1)) {			
			for (int i = 0; i < idcs.length; i++) {
				Object obj = model.remove(idcs[i]);
				idcs[i] = idcs[i] + 1;
				model.insertElementAt(obj,idcs[i]);
			}
			mainList.setSelectedIndices(idcs);
		}		
	}
	
	private void turnSelectionIntoSinglePages() throws IOException {		
		int[] idcs = mainList.getSelectedIndices();
		DefaultListModel model = ((DefaultListModel)mainList.getModel());
		if (idcs.length == 0) { return; }
		else {			
			for (int i = 0; i < idcs.length; i++) {
				PDFPages item = (PDFPages)model.get(idcs[i]);
				item.setRange(1,1);
				int addedpages = 0;
				for (int j = 2; j <= item.getNumPages(); j++) {
					model.insertElementAt(new PDFPages(counter++, new File(item.getFile().getCanonicalPath()), j, j), idcs[i]+j-1);
					addedpages++;
				}
				for (int j = i+1; j < idcs.length; j++) { idcs[j] = idcs[j] + addedpages; }				
			}
			mainList.setSelectedIndices(idcs);
		}
	}

	public void actionPerformed(ActionEvent e) {
		
		this.setStatus(null);
		
		if (e.getSource() instanceof JButton) {
			
			if (e.getSource() == bRemove) {
				
				this.removeSelectedItens();
				
			} else if (e.getSource() == bDuplicate) {
				
				int idx = mainList.getSelectedIndex();
				if (idx >= 0) { 
					DefaultListModel model = ((DefaultListModel)mainList.getModel());
					PDFPages newitem;
					try {
						newitem = new PDFPages(counter,((PDFPages)model.getElementAt(idx)).getFile());
						model.add(idx+1,newitem); 
						counter++;
					} catch (IOException e1) {
						e1.printStackTrace();
					} 				
				}
				
			} else if (e.getSource() == bUp) {
				
				this.moveItensUp();
				
			} else if (e.getSource() == bDown) {
				
				this.moveItensDown();
				
			} else if (e.getSource() == bSplit) {
				
				int idx = mainList.getSelectedIndex();		
				if (idx >= 0) { 
					PDFPages item = (PDFPages)mainList.getSelectedValue();
					if (item.getNumPages() > 1) {
						// TODO: Fazer Split!!!					
					}
				}
				
			} else if (e.getSource() == bSinglePages) {
				
				int idx = mainList.getSelectedIndex();
				if (idx >= 0) { 
					PDFPages item = (PDFPages)mainList.getSelectedValue();
					if (item.getNumPages() > 1) {
						try {
							this.turnSelectionIntoSinglePages();
						} catch (IOException e1) {
							e1.printStackTrace();
						}					
					}
				}
								
			} else if (e.getSource() == bBuild) {
				
				try {
					this.generateOutputFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (DocumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
		} else if (e.getSource() instanceof JMenuItem) {
			
			if (e.getSource() == menuOpen) {

				try {
					this.openListFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				
			} else if (e.getSource() == menuSave) {
				
				try {
					this.saveListFile(true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			} else if (e.getSource() == menuSaveAs) {
				
			} else if (e.getSource() == menuExit) {
				// TODO: Check if file was saved / change exit method to do the same as X button.
				System.exit(0);
			} else if (e.getSource() == menuAbout) {
				
			}
			
		}
		
		
		
	}
	
	

}
