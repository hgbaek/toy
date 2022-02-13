package data.xml;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

/**
 * 개요 : <br>
 * 작성일 : 2016. 5. 14.<br>
 * 작성자 : 민경현<br>
 * Version : 1.00
 */
public class SgXmlWriter 
{
	TransformerFactory transformerFactory;
	Transformer transformer;
	StreamResult result;
	
	DOMSource source;
	Document document;
	URI uri;
	
	String path;
	String fileName;
	
	public SgXmlWriter(Document document, URI uri)
	{
		this.document = document;
		this.uri = uri;
	}
	
	public SgXmlWriter(Document document, String path, String fileName)
	{
		this.document = document;
		this.path = path;
		this.fileName = fileName;
	}
	
	public void save()
	{
		if(uri != null)
		{
			saveUri();
		}
		else if(path != null)
		{
			savePath();
		}
	}
	
	public void saveUri()
	{
		try
		{
			// XML 파일로 쓰기
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");	
			source = new DOMSource(document);
			
			result =  new StreamResult(new FileOutputStream(new File(uri)));
			transformer.transform(source, result);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public void savePath()
	{
		try 
		{
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			source = new DOMSource(document);
			result = new StreamResult(new FileOutputStream(new File(path + fileName + ".cfg")));
			transformer.transform(source, result);
			System.out.println(fileName + "File saved");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}

