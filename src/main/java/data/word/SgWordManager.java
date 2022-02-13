package data.word;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 개요 : <br>
 * 작성일 : 2016. 12. 6.<br>
 * 작성자 : 민경현<br>
 * Version : 1.00
 */
public class SgWordManager 
{
	public SgWordManager()
	{
		ini();
	}
	
	private void ini()
	{
		try
		{
			XWPFDocument validation = new XWPFDocument(new FileInputStream(new File("d:\\validation.docx")));
			XWPFStyles stylesVal = validation.getStyles();

			XWPFDocument document = new XWPFDocument();
			XWPFStyles stylesDoc = document.createStyles();
			
			InputStream is = stylesVal.getPackagePart().getInputStream();
			StylesDocument stylesValDoc = StylesDocument.Factory.parse(is);
			stylesDoc.setStyles(stylesValDoc.getStyles());
			
			for(int i=0; i<validation.getRelations().size(); i++)
			{
				POIXMLDocumentPart part = validation.getRelations().get(i);
				document.addRelation(part.getPackageRelationship().getId(), part);
			}
			
			for(int i=0; i<260; i++)
			{
				IBodyElement element = validation.getBodyElements().get(i);
				if(element instanceof XWPFParagraph)
				{
					XWPFParagraph paragraph = (XWPFParagraph)element;
					if(stylesVal.getStyle(paragraph.getStyleID()) != null)
					{
						stylesDoc.addStyle(stylesVal.getStyle(paragraph.getStyleID()));
					}
					document.createParagraph();
					document.setParagraph(paragraph, document.getParagraphs().size()-1);
				}
				if(element instanceof XWPFTable)
				{
					XWPFTable table = (XWPFTable)element;
					if(stylesVal.getStyle(table.getStyleID()) != null)
					{
						stylesDoc.addStyle(stylesVal.getStyle(table.getStyleID()));
					}
					document.createTable();
					document.setTable(document.getTables().size()-1, table);
				}
			}
			
			for(int i=0; i<validation.getAllPictures().size(); i++)
			{
				XWPFPictureData data = validation.getAllPictures().get(i);
				document.addPictureData(data.getData(), data.getPictureType());
			}
			
			validation.write(new FileOutputStream(new File("d:\\test.docx")));
			document.write(new FileOutputStream(new File("d:\\test3.docx")));
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args)
	{
		new SgWordManager();
	}
}