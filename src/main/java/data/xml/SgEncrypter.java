package data.xml;

import org.apache.xml.security.Init;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SgEncrypter 
{
	private static final String AES_KEY = "20190708_ENC_KEY_SV";
	Key symmetricKey;
	Key keyEncryptKey; 
	
	static 
	{
		Init.init();
	}
	
	public SgEncrypter(String keyPosition)
	{
		// dhseol 2019-07-08 : 저장할때 마다 키가 생성되는 것이 아닌, 프로그래머가 원할때 키가 생성/변경 되도록 로직 수정
		this.symmetricKey = GenerateSymmetricKey();
		// dhseol 2019-07-08 : testkit 파일을 매번 생성하는 것이 아닌, 기존에 생성된 testkit 파일을 불러와서 사용할 수 있도록 로직 변경
		this.keyEncryptKey = SgDecrypter.loadKeyEncryptionKey(keyPosition+"\\testkit");// GenerateKeyEncryptionKey();
		storeKeyFile(keyEncryptKey, keyPosition);
	}
	/*
	public SgEncrypter()
	{
		this.symmetricKey = GenerateSymmetricKey();
		this.keyEncryptKey = GenerateKeyEncryptionKey();
		storeKeyFile(this.keyEncryptKey);
	}
	*/
	
	/**
	 *  키 암호화 키를 저장하려면 키 암호화 키를 파일에 쓰는 다음 방법을 사용하십시오. 
	 * 그런 다음 해독 도구는 나중에 이를 검색하여이를 사용하여 암호화 된 데이터의 암호를 해독 할 수 있습니다.
	 * @param keyEncryptKey
	 */
	private void storeKeyFile(Key keyEncryptKey, String keyPosition)
	{ 
		String fileName = "testkit";
		String keyPath = keyPosition+"\\"+fileName;
		byte[] keyBytes = keyEncryptKey.getEncoded();
		File keyEncryptKeyFile = new File(keyPath);
		FileOutputStream outStream;
		try 
		{
			outStream = new FileOutputStream(keyEncryptKeyFile);
			outStream.write(keyBytes); 
			outStream.close();
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Key encryption key stored in: " + keyEncryptKeyFile.toString()); 
	}
	
	public void encryption(Document document, String path, String fileName)
	{
		try
		{
			// 키 암호화 키 WRAP_MODE 설정
			XMLCipher keyCipher = XMLCipher.getInstance( XMLCipher.TRIPLEDES_KeyWrap); 
			keyCipher.init(XMLCipher.WRAP_MODE, keyEncryptKey); 
			EncryptedKey encryptedKey = keyCipher.encryptKey(document, symmetricKey);
			
			// 암호화 할 요소를 지정
			Element rootElement = document.getDocumentElement();
			Element elementToEncrypt = rootElement; 
			
			// 데이터 암호화 키를 사용하여 암호를 생성하고 초기화합니다.
			XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128);
			xmlCipher.init( XMLCipher.ENCRYPT_MODE, symmetricKey);
			
			// 암호화 된 키의 키 정보를 암호화 된 데이터 요소에 추가
			EncryptedData encryptedDataElement = xmlCipher.getEncryptedData(); 
			KeyInfo keyInfo = new KeyInfo(document);
			keyInfo.add(encryptedKey);
			encryptedDataElement.setKeyInfo(keyInfo);
			
			// 실제 암호화를 수행
			boolean encryptContentsOnly = true;
			xmlCipher.doFinal(document, elementToEncrypt, encryptContentsOnly);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		writeEncryptedDocToFile(document, path, fileName);
	}
	
	// 암호화 할 Document 만들어서 반환한다. 
	public Document creatDocument()
	{
		Document document = null;
		
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			dbf.setNamespaceAware(true); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.newDocument();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return document;
	}
	
	// 암호화 할 XML 파일을 불러와 구문을 분석하고 Document 반환한다.
	public Document parseFile(String fileName)
	{
		Document document = null;
		
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
			dbf.setNamespaceAware(true); 
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(fileName); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return document;
	}
	
//	/**
//	 * 데이터 암호화 도구를 암호화하는 데 사용되는 키를 생성해야합니다. 
//	생성 된 키는 데이터 암호화 도구를 안전하게 저장 및 또는 전송하는 데 필요합니다.
//	 * @return SecretKey
//	 */
//	private static SecretKey GenerateKeyEncryptionKey()
//	{ 
//		String jceAlgorithmName = "DESede"; 
//		KeyGenerator keyGenerator = null;
//		try
//		{
//			keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
//		} 
//		catch (NoSuchAlgorithmException e) 
//		{
//			e.printStackTrace();
//		} 
//		SecretKey keyEncryptKey = keyGenerator.generateKey(); 
//		return keyEncryptKey; 
//	}


//	/**
//	 *  키 암호화 키를 저장하려면 키 암호화 키를 파일에 쓰는 다음 방법을 사용하십시오. 
//	 * 그런 다음 해독 도구는 나중에 이를 검색하여이를 사용하여 암호화 된 데이터의 암호를 해독 할 수 있습니다.
//	 * @param keyEncryptKey
//	 */
//	private static void storeKeyFile(Key keyEncryptKey)
//	{ 
//		byte[] keyBytes = keyEncryptKey.getEncoded();
//		File keyEncryptKeyFile = new File("keyEncryptKey"); 
//		FileOutputStream outStream;
//		try 
//		{
//			outStream = new FileOutputStream(keyEncryptKeyFile);
//			outStream.write(keyBytes); 
//			outStream.close();
//		}
//		catch (Exception e) 
//		{
//			e.printStackTrace();
//		}
//
//		// System.out.println("Key encryption key stored in: " + keyEncryptKeyFile.toString()); 
//	}
	
	/**
	 * 다음 방법을 사용하여 대칭 데이터 암호화 키를 생성합니다.
	 * @return SecretKey
	 */
	private static SecretKey GenerateSymmetricKey()
	{ 
		String jceAlgorithmName = "AES";
		KeyGenerator keyGenerator = null;
		SecretKey returnValue = null;
		try 
		{
			keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed( SgEncrypter.AES_KEY.getBytes() );
			keyGenerator.init(128, sr);
			
			returnValue = keyGenerator.generateKey();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		
		byte[] raw = returnValue.getEncoded();
		for( byte t : raw ) {
			System.out.print( t );
		}
		System.out.println();
		return returnValue;
	}

	
	/**
	 * 암호화 된 문서를 파일에 씁니다.
	 * @param doc
	 * @param fileName
	 * @param path
	 */
	public static void writeEncryptedDocToFile( Document doc, String path, String fileName)
	{ 
		File encryptionFile = new File(path + fileName + ".cfg"); 
		FileOutputStream outStream;
		try 
		{
			outStream = new FileOutputStream(encryptionFile);
			TransformerFactory factory = TransformerFactory.newInstance(); 
			Transformer transformer = factory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			DOMSource source = new DOMSource(doc); 
			StreamResult result = new StreamResult(outStream); 
			transformer.transform(source, result); 
			outStream.close(); 
			// System.out.println( "Encrypted XML document written to: " + encryptionFile.toString());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}
	
	/**
	 * 암호화 된 문서를 파일에 씁니다.
	 * @param doc
	 * @param fileName
	 * @param path
	 */
	public static void writeEncryptedDocToFile( Document doc, String fileName)
	{ 
		File encryptionFile = new File(fileName); 
		FileOutputStream outStream;
		try 
		{
			outStream = new FileOutputStream(encryptionFile);
			TransformerFactory factory = TransformerFactory.newInstance(); 
			Transformer transformer = factory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			DOMSource source = new DOMSource(doc); 
			StreamResult result = new StreamResult(outStream); 
			transformer.transform(source, result); 
			outStream.close(); 
			// System.out.println( "Encrypted XML document written to: " + encryptionFile.toString());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}
}
