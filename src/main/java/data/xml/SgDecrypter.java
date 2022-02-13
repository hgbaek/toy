package data.xml;

import org.apache.xml.security.Init;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.apache.xml.security.utils.JavaUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Key;

public class SgDecrypter 
{
	static 
	{
		Init.init();
	}

	public Document decryption(String fileName, boolean saveFlag) 
	{
		// 먼저 XML 파일을 검색하여 DOM 문서로 해독하고 파싱		
		Document document = loadEncryptedFile(fileName);
		
		// DOM 문서에서 암호화 된 데이터 요소를 검색
		String namespaceURI = EncryptionConstants.EncryptionSpecNS;
		String localName = EncryptionConstants._TAG_ENCRYPTEDDATA;
		Element encryptedDataElement = (Element) document.getElementsByTagNameNS(namespaceURI, localName).item(0);

		// 디스크에서 데이터 암호 해독 키를 해독하는 데 사용할 키를 검색
		Key keyEncryptKey = loadKeyEncryptionKey();
		
		// 데이터 암호 해독 키를 사용하여 암호를 생성하고 초기화합니다. 
		// 이 경우 모드는 데이터를 해독하는 데 사용되므로 DECRYPT_MODE로 설정
		try
		{
			XMLCipher xmlCipher = XMLCipher.getInstance();
			xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
			xmlCipher.setKEK(keyEncryptKey);
			
			// 암호 해독을 수행
			xmlCipher.doFinal(document, encryptedDataElement);
			
			// 암호화 해독 후 파일 저장
			if(saveFlag == true) writeDecryptedDocTofile(document, fileName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return document;
	}
	
	public Document decryption(String fileName, String keyFileName, boolean saveFlag) 
	{
		// 먼저 XML 파일을 검색하여 DOM 문서로 해독하고 파싱
		Document document = loadEncryptedFile(fileName);
		
		// DOM 문서에서 암호화 된 데이터 요소를 검색
		String namespaceURI = EncryptionConstants.EncryptionSpecNS;
		String localName = EncryptionConstants._TAG_ENCRYPTEDDATA;
		Element encryptedDataElement = (Element) document.getElementsByTagNameNS(namespaceURI, localName).item(0);

		// 디스크에서 데이터 암호 해독 키를 해독하는 데 사용할 키를 검색
		Key keyEncryptKey = loadKeyEncryptionKey(keyFileName);
		
		// 데이터 암호 해독 키를 사용하여 암호를 생성하고 초기화합니다. 
		// 이 경우 모드는 데이터를 해독하는 데 사용되므로 DECRYPT_MODE로 설정
		try
		{
			XMLCipher xmlCipher = XMLCipher.getInstance();
			xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
			xmlCipher.setKEK(keyEncryptKey);
			
			// 암호 해독을 수행
			xmlCipher.doFinal(document, encryptedDataElement);
			
			// 암호화 해독 후 파일 저장
			if(saveFlag == true) writeDecryptedDocTofile(document, fileName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return document;
	}
	
	private Document loadEncryptedFile(String fileName)
	{
		File encryptedFile = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		
		Document document = null;
		try
		{
			DocumentBuilder builder = dbf.newDocumentBuilder();
			document = builder.parse(encryptedFile);
			
			// System.out.println( "Encryption document loaded from: " + encryptedFile.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		 
		return document;
	}
	
	private static SecretKey loadKeyEncryptionKey()
	{
		String fileName = "keyEncryptKey";
		String jceAlgorithmName = "DESede";
		
		SecretKey key = null;
		try
		{
			DESedeKeySpec keySpec = new DESedeKeySpec(JavaUtils.getBytesFromFile(fileName));
			SecretKeyFactory skf =SecretKeyFactory.getInstance(jceAlgorithmName);
			key = skf.generateSecret(keySpec);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return key;		
	}
	
	public static SecretKey loadKeyEncryptionKey(String fileName)
	{
		String jceAlgorithmName = "DESede";
		
		// File kekFile = new File(fileName);
		
		SecretKey key = null;
		try
		{
			DESedeKeySpec keySpec = new DESedeKeySpec(JavaUtils.getBytesFromFile(fileName));
			SecretKeyFactory skf =SecretKeyFactory.getInstance(jceAlgorithmName);
			key = skf.generateSecret(keySpec);
			
			// System.out.println("Key encryption key loaded from: " + fileName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return key;		
	}
	
	private static void writeDecryptedDocTofile(Document doc, String fileName)
	{
		File encryptionFile = new File(fileName + "_A");
		
		try
		{
			FileOutputStream outStream = new FileOutputStream(encryptionFile);
			
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outStream);
			transformer.transform(source, result);
			
			outStream.close();
			// System.out.println("Decrypted data written to: " + encryptionFile.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
