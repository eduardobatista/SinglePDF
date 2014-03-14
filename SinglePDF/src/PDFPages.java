import java.io.File;
import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;


public class PDFPages {
	
	private int id;
	private File pdfFile;
	private int[] range;
	private int numpages;
	
	public PDFPages(int id, File file) throws IOException {
		this.id = id;
		this.pdfFile = file;
		this.numpages = 1;
		this.range = new int[] {1, 1};
		PdfReader reader = new PdfReader(file.getAbsolutePath());
		this.numpages = reader.getNumberOfPages();
		reader.close();
		this.range = new int[] {1, this.numpages};		
	}
	
	public PDFPages(int id, File file, int startPage, int endPage) throws IOException {		
		this.id = id;
		this.pdfFile = file;		
		PdfReader reader = new PdfReader(file.getAbsolutePath());
		this.numpages = reader.getNumberOfPages();
		reader.close();
		if ( (startPage <= this.numpages) && (endPage <= this.numpages) ) {
			this.range = new int[] {startPage, endPage};
		} else {
			this.range = new int[] {1, this.numpages};
		}	
	}
	
	public File getFile() {
		return pdfFile;
	}
	
	public String getFileName() {
		return pdfFile.getName();
	}
	
	public int getId() {
		return id;
	}
	
	public String getAbsolutePath() {
		return pdfFile.getAbsolutePath();
	}
	
	public void setRange(int start, int end) {
		this.range = new int[]{start,end};
	}
	
	public int getNumPages() {
		return this.numpages;
	}
	
	public int getStart() {
		return this.range[0];
	}
	public int getEnd() {
		return this.range[1];
	}
	
	@Override
	public String toString() {
		if ( (range[0] == -1) || (range[1] == -1) ) {
			return pdfFile.getName();
		} else {
			return "<html>" + pdfFile.getName() + "<br>Pages " + Integer.toString(range[0]) + " to " + Integer.toString(range[1]) + " of " + Integer.toString(numpages) +  "</html>";
		}
		
	}

}
