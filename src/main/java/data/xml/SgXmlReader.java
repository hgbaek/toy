package data.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import data.SgEncryption;
import data.SgProperties;

import javax.crypto.spec.GCMParameterSpec;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;

/**
 * 개요 : <br>
 * 작성일 : Nov 26, 2012<br>
 * 작성자 : 민경현<br>
 * Version : 1.00
 */
public class SgXmlReader 
{
	public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	public static DocumentBuilder builder;
	private Document doc;
	private Node rootNode;
	
//	private static Logger LOGGER=LogManager.getLogger(SgXmlReader.class);
	private static final String LOGMSG_LOADXML_FAILED = "Load XML failed.";
	private static final String LOGMSG_MAKEINSTANCE_FAILED = "Initialize a XML parser instance failed.";
	private static final String LOGMSG_WRITEXML_FAILED = "Write XML failed.";
	/**
	 * 생성자
	 * @ param xmlPath
	 */
	public SgXmlReader(InputStream stream)  throws Exception
	{
		try
		{
			if(builder == null)
			{
				factory.setValidating(false);
				factory.setIgnoringComments(false);
				factory.setIgnoringElementContentWhitespace(true);
				factory.setNamespaceAware(true);
				builder = factory.newDocumentBuilder();
			}
			doc = builder.parse(stream);
			rootNode = doc.getFirstChild();
			//stream.close(); // 2021.11.22 HHGWON 정적 분석 : finally로 이동
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally {
			stream.close(); // 2021.11.22 HHGWON 정적 분석 : finally로 이동
		}
	}
	
	/**
	 * 생성자
	 * @ param xmlPath
	 */
	public SgXmlReader(URL xmlPath) 
	{
		try 
		{
			if(builder == null)
			{
				factory.setValidating(false);
				factory.setIgnoringComments(false);
				factory.setIgnoringElementContentWhitespace(true);
				factory.setNamespaceAware(true);
				builder = factory.newDocumentBuilder();
			}
			doc = builder.parse(xmlPath.openStream());
			rootNode = doc.getFirstChild();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * 생성자
	 * AES128 ECB 암호화된 XML 파일로부터 읽기 수행
	 *
	 * @ param xmlPath XML URL
	 * @ param aes128Key String 형태의 AES128 키
	 */
	public SgXmlReader(URL xmlPath, String aes128Key)
	{
		try(InputStream is = SgEncryption.getAES128DecodeInputStream(aes128Key, xmlPath.openConnection().getInputStream()))
		{
			doc = builder.parse(is);
			rootNode = doc.getFirstChild();
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_LOADXML_FAILED, e);
		}
	}
	/**
	 * 생성자
	 * AES128 GCM 암호화된 XML 파일로부터 읽기 수행
	 *
	 * @ param xmlfile	XML 파일의 File
	 * @ param aes128Key	AES128 키
	 */
	public SgXmlReader(File xmlfile, byte[] aes128Key)
	{
		try(InputStream stream = SgEncryption.getAES128DecodeInputStream(aes128Key, xmlfile))
		{
			doc = builder.parse(stream);
			rootNode = doc.getFirstChild();
			
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_LOADXML_FAILED, e);
		}
	}
	/**
	 * 생성자
	 * @ param doc
	 */
	public SgXmlReader(Document doc)
	{
		this.doc = doc;
		this.rootNode = doc.getFirstChild();
	}
	
	/**
	 * 생성자
	 * @ param rootNode
	 */
	public SgXmlReader(Node rootNode)
	{
		this.rootNode = rootNode;
		this.doc = rootNode.getOwnerDocument();
	}
	
	/**
	 * 생성자
	 * 새로 xml 데이터를 생성합니다.
	 * @ param root
	 */
	public SgXmlReader(String root)
	{
		try
		{
			if(builder == null)
			{
				factory.setValidating(false);
				factory.setIgnoringComments(false);
				factory.setIgnoringElementContentWhitespace(true);
				factory.setNamespaceAware(true);
				builder = factory.newDocumentBuilder();
			}
			doc = builder.newDocument();
			rootNode = doc.createElement(root);
			doc.appendChild(rootNode);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 생성자
	 * 새로 xml 데이터를 생성합니다.
	 * @ param root
	 * @ param attributes
	 */
	public SgXmlReader(String root, ArrayList<SgProperties> attributes)
	{
		try
		{
			if(builder == null)
			{
				factory.setValidating(false);
				factory.setIgnoringComments(false);
				factory.setIgnoringElementContentWhitespace(true);
				factory.setNamespaceAware(true);
				builder = factory.newDocumentBuilder();
			}
			doc = builder.newDocument();
			rootNode = doc.createElement(root);
			doc.appendChild(rootNode);
			for(SgProperties attribute : attributes)
			{
				if(rootNode instanceof Element)
					((Element)rootNode).setAttribute(attribute.getKey(), attribute.getStringValue());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ param attributes
	 * @ return
	 */
	public SgXmlReader createChildNode(SgXmlReader node)
	{
		SgXmlReader childNode = null;
		// 하위 node 의 context 를 가져오는 오류 확인.
		if(node.getChildNodes() != null && node.getChildNodes().size() > 0)
		{
			childNode = createChildNode(node.getLocalName(), node.getAttributes());
		}
		else
		{
			childNode = createChildNode(node.getLocalName(), node.getTextContent().trim(), node.getAttributes());
		}
		for(int i=0; i<node.getChildNodes().size(); i++)
		{
			childNode.createChildNode(node.getChildNodes().get(i));
		}
		return childNode;
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ return
	 */
	public SgXmlReader createChildNode(String tagName)
	{
		Element node = doc.createElement(tagName);
		rootNode.appendChild(node);
		return new SgXmlReader(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ param content
	 * @ return
	 */
	public SgXmlReader createChildNode(String tagName, String content)
	{
		Element node = doc.createElement(tagName);
		if(content != null)
		{
			node.setTextContent(content);
		}
		rootNode.appendChild(node);
		return new SgXmlReader(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ param attribute
	 * @ return
	 */
	public SgXmlReader createChildNode(String tagName, SgProperties attribute)
	{
		Element node = doc.createElement(tagName);
		node.setAttribute(attribute.getKey(), attribute.getStringValue());
		rootNode.appendChild(node);
		return new SgXmlReader(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ param attributes
	 * @ return
	 */
	public SgXmlReader createChildNode(String tagName, ArrayList<SgProperties> attributes)
	{
		Element node = doc.createElement(tagName);
		for(SgProperties attribute : attributes)
		{
			node.setAttribute(attribute.getKey(), attribute.getStringValue());
		}
		rootNode.appendChild(node);
		return new SgXmlReader(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ param content
	 * @ param attributes
	 * @ return
	 */
	public SgXmlReader createChildNode(String tagName, String content, ArrayList<SgProperties> attributes)
	{
		Element node = doc.createElement(tagName);
		if(content != null)
		{
			node.setTextContent(content);
		}
		for(SgProperties attribute : attributes)
		{
			node.setAttribute(attribute.getKey(), attribute.getStringValue());
		}
		rootNode.appendChild(node);
		return new SgXmlReader(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param tagName
	 * @ param content
	 * @ param attribute
	 * @ return
	 */
	public SgXmlReader createChildNode(String tagName, String content, SgProperties attribute)
	{
		Element node = doc.createElement(tagName);
		if(content != null)
		{
			node.setTextContent(content);
		}
		node.setAttribute(attribute.getKey(), attribute.getStringValue());
		rootNode.appendChild(node);
		return new SgXmlReader(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param node
	 */
	public void addChildNode(Node node)
	{
		rootNode.appendChild(node);
	}
	
	/**
	 * Root Node 의 ChildNode 를 생성합니다.
	 * @ param node
	 */
	public void addChildNode(SgXmlReader node)
	{
		rootNode.appendChild(node.getRootNode());
	}
	
	/**
	 * Root Node 의 ChildNode 를 반환합니다.
	 * @ return
	 */
	public ArrayList<SgXmlReader> getChildNodes()
	{
		ArrayList<SgXmlReader> childNodes = new ArrayList<SgXmlReader>();
		for(int i=0; i<rootNode.getChildNodes().getLength(); i++)
		{
			Node childNode = rootNode.getChildNodes().item(i);
			if(childNode != null && childNode.getLocalName() != null)
			{
				childNodes.add(new SgXmlReader(childNode));
			}
		}
		return childNodes;
	}
	
	/**
	 * LocalName 에 해당하는 Node 를 검색하여 반환합니다.
	 * @ param localName
	 * @ return
	 */
	public SgXmlReader searchChildNode(String localName)
	{
		return searchChildNode(rootNode, localName);
	}
	
	/**
	 * LocalName 에 해당하는 Node 를 검색하여 리스트로 반환합니다.
	 * @ param localName
	 * @ return
	 */
	public ArrayList<SgXmlReader> searchChildNodes(String localName)
	{
		return searchChildNodes(rootNode, localName);
	}
	
	/**
	 * Node 의 ChildNode 중 LocalName 에 해당하는 Node 를 검색하여 리스트로 반환합니다.
	 * @ param parentNode
	 * @ param localName
	 * @ return
	 */
	private ArrayList<SgXmlReader> searchChildNodes(Node parentNode, String localName)
	{
		ArrayList<SgXmlReader> returnNodeList = new ArrayList<SgXmlReader>();
		if(parentNode != null)
		{
			NodeList childNodes = parentNode.getChildNodes();
			for(int i=0; i<childNodes.getLength(); i++)
			{
				// 검색 조건이 null 이면 null 이 설정된 Node 를 추가합니다.
				if(localName == null && childNodes.item(1).getLocalName() == null)
				{
					returnNodeList.add(new SgXmlReader(childNodes.item(i)));
				}
				else
				{
					//  Local Name 이 같은 Node 를 추가합니다.
					if(localName != null && localName.equals(childNodes.item(i).getLocalName()))
					{
						returnNodeList.add(new SgXmlReader(childNodes.item(i)));
					}
					else if(localName != null && localName.equals(childNodes.item(i).getNodeName()))
					{
						returnNodeList.add(new SgXmlReader(childNodes.item(i)));
					}
				}
			}
		}
		return returnNodeList;
	}
	
	/**
	 * Node 의 ChildNode 중 LocalName 에 해당하는 Node 를 검색하여 리스트로 반환합니다.
	 * @ param parentNode
	 * @ param localName
	 * @ return
	 */
	private SgXmlReader searchChildNode(Node parentNode, String localName)
	{
		NodeList childNodes = parentNode.getChildNodes();
		for(int i=0; i<childNodes.getLength(); i++)
		{
			// 검색 조건이 null 이면 node 에 null 이 설정된 것을 찾아서 추가합니다.
			if(localName == null && childNodes.item(1).getLocalName() == null)
			{
				return new SgXmlReader(childNodes.item(i));
			}
			else
			{
				//  Local Name 이 같은 Node 를 추가합니다.
				if(localName != null && localName.equals(childNodes.item(i).getLocalName()))
				{
					return new SgXmlReader(childNodes.item(i));
				}
			}
		}
		return null;
	}
	
	/**
	 * attribute id 의 값이 value 와 같은 Node 를 반환합니다. 
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public SgXmlReader searchChildNode(String attributeId, String value)
	{
		return searchChildNode(rootNode.getChildNodes(), attributeId, value);
	}
	
	/**
	 * attribute id 값이 value 와 같은 Node 의 리스트를 반환합니다.
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public ArrayList<SgXmlReader> searchChildNodes(String attributeId, String value)
	{
		return searchChildNodes(rootNode.getChildNodes(), attributeId, value);
	}
	
	/**
	 * attribute id 의 값이 value 와 같은 Node 를 반환합니다. 
	 * @ param localName
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public SgXmlReader searchChildNode(String localName, String attributeId, String value)
	{
		ArrayList<SgXmlReader> searchNodes = searchChildNodes(localName);
		return searchChildNode(searchNodes, attributeId, value);
	}
	
	/**
	 * attribute id 값이 value 와 같은 Node 의 리스트를 반환합니다.
	 * @ param localName
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public ArrayList<SgXmlReader> searchChildNodes(String localName, String attributeId, String value)
	{
		ArrayList<SgXmlReader> searchNodes = searchChildNodes(localName);
		return searchChildNodes(searchNodes, attributeId, value);
	}
	
	/**
	 * attribute id 의 값이 value 와 같은 Node 를 반환합니다. 
	 * @ param childNodes
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public SgXmlReader searchChildNode(NodeList childNodes, String attributeId, String value)
	{
		for(int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			String textContent = node.getAttributes().getNamedItem(attributeId).getTextContent();
			if(value != null && value.equals(textContent))
			{
				return new SgXmlReader(node);
			}
		}
		return null;
	}
	
	/**
	 * attribute id 값이 value 와 같은 Node 의 리스트를 반환합니다.
	 * @ param childNodes
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public ArrayList<SgXmlReader> searchChildNodes(NodeList childNodes, String attributeId, String value)
	{
		ArrayList<SgXmlReader> searchNodeList = new ArrayList<SgXmlReader>();
		for(int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			String textContent = node.getAttributes().getNamedItem(attributeId).getTextContent();
			if(value != null && value.equals(textContent))
			{
				searchNodeList.add(new SgXmlReader(node));
			}
		}
		return searchNodeList;
	}
	
	/**
	 * attribute id 의 값이 value 와 같은 Node 를 반환합니다.
	 * @ param childNodes
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public SgXmlReader searchChildNode(ArrayList<SgXmlReader> childNodes, String attributeId, String value)
	{
		for(int i=0; i<childNodes.size(); i++)
		{
			SgXmlReader node = childNodes.get(i);
			String textContent = node.getAttributeValue(attributeId);
			if(value != null && value.equals(textContent))
			{
				return node;
			}
		}
		return null;
	}
	
	/**
	 * attribute id 값이 value 와 같은 Node 의 리스트를 반환합니다.
	 * @ param childNodes
	 * @ param attributeId
	 * @ param value
	 * @ return
	 */
	public ArrayList<SgXmlReader> searchChildNodes(ArrayList<SgXmlReader> childNodes, String attributeId, String value)
	{
		ArrayList<SgXmlReader> searchNodeList = new ArrayList<SgXmlReader>();
		for(int i=0; i<childNodes.size(); i++)
		{
			SgXmlReader node = childNodes.get(i);
			String attributeValue = node.getAttributeValue(attributeId);
			if(value != null && value.equals(attributeValue))
			{
				searchNodeList.add(node);
			}
		}
		return searchNodeList;
	}
	
	/**
	 * attribute id 에 해당하는 value 값을 반환합니다.
	 * @ param node
	 * @ param attributeId
	 * @ return
	 */
	public String getAttributeValue(Node node, String attributeId)
	{
		Node itemNode = node.getAttributes().getNamedItem(attributeId);
		if(itemNode != null)
		{
			return itemNode.getTextContent();
		}
		return null;
	}
	
	/**
	 * attribute id 에 해당하는 value 값을 반환합니다.
	 * @ param attributeId
	 * @ return
	 */
	public String getAttributeValue(String attributeId)
	{
		return getAttributeValue(rootNode, attributeId);
	}
	
	/**
	 * attribute list 를 반환합니다.
	 * @ return
	 */
	public ArrayList<SgProperties> getAttributes()
	{
		ArrayList<SgProperties> attributes = new ArrayList<SgProperties>();
		for(int i=0; i<getRootNode().getAttributes().getLength(); i++)
		{
			Node node = getRootNode().getAttributes().item(i);
			attributes.add(new SgProperties(node.getNodeName(), node.getNodeValue()));
		}
		return attributes;
	}
	
	public NodeList getNodeList(String tagName)
	{
		return ((Element) rootNode).getElementsByTagName(tagName);
	}
	
	public Node getNode(String nodeName)
	{
		for(int i = 0; i < rootNode.getChildNodes().getLength(); i++)
		{
			if(rootNode.getChildNodes().item(i).getNodeName().equals(nodeName))
			{
				return ((Element)rootNode).getChildNodes().item(i);
			}
		}
		return null;
	}

	public void setNodeValue(String nodeName, String value)
	{
		try {
			Node node = getNode(nodeName);
			if (node != null) node.setNodeValue(value);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setNodeValue(String value)
	{
		try {
			rootNode.getFirstChild().setNodeValue(value);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public NodeList getNodeList(Node node, String tagName)
	{
		return  ((Element) node).getElementsByTagName(tagName);
	}
	
	public String getTextContent(String tagName)
	{
		return ((Element) rootNode).getElementsByTagName(tagName).item(0).getTextContent();
	}
	
	public String getAttribute(String nameItem)
	{
		return rootNode.getAttributes().getNamedItem(nameItem).getTextContent();
	}

	public Node getNamedItem(String nameItem)
	{
		return rootNode.getAttributes().getNamedItem(nameItem);
	}

	public void setAttributeText(String nameItem, String value)
	{
		rootNode.getAttributes().getNamedItem(nameItem).setTextContent(value);
	}

	public void setRootNode(Node rootNode) 
	{
		this.rootNode = rootNode;
	}

	/**
	 * Text Content 값을 반환합니다.
	 * @ return
	 */
	public String getTextContent()
	{
		return rootNode.getTextContent();
	}
	
	/**
	 * Local Name 값을 반환합니다.
	 * @ return
	 */
	public String getLocalName()
	{
		return rootNode.getLocalName();
	}
	
	/**
	 * Tag Name 값을 반환합니다.
	 * @ return
	 */
	public String getNodeName()
	{
		return rootNode.getNodeName();
	}
	
	/**
	 * xml 내용을 stream 에 입력합니다.
	 * @ param out
	 */
	public void write(OutputStream out)
	{
		try
		{
			out.write(toString().getBytes("UTF-8"));
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * xml 파일을 생성합니다.
	 *   filePath
	 */
	public void write(String filePath)
	{
		try
		{
			File file = new File(filePath);
			file.setWritable(true);
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			out.write(toString().getBytes("UTF-8"));
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * xml 파일을 생성합니다.
	 * @ param url
	 */
	public void write(URL url)
	{
		try
		{
			File file = new File(url.toURI());
			file.setWritable(true);
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			out.write(toString().getBytes("UTF-8"));
			out.close();
		}
		catch(Exception e)
		{
//			e.printStackTrace();
		}
	}
	
	/**
	 * xml 파일을 생성합니다.
	 *
	 * @ param url
	 */
	public void writeEncodeAES128(String filePath, String key)
	{
		writeEncodeAES128(new File(filePath), key);
	}

	/**
	 * xml 파일을 생성합니다.
	 *
	 * @ param url
	 */
	public void writeEncodeAES128(URL url, String key)
	{
		try
		{
			writeEncodeAES128(new File(url.toURI()), key);
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_WRITEXML_FAILED, e);
		}
	}

	/**
	 * xml 파일을 생성합니다.
	 *
	 * @ param file
	 */
	public void writeEncodeAES128(File file, String key)
	{
		try
		{
			file.setWritable(true);
			file.createNewFile();
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_WRITEXML_FAILED, e);
		}
		try(FileOutputStream out = new FileOutputStream(file))
		{
			out.write(SgEncryption.getAES128Encode(key, toString()).getBytes(StandardCharsets.UTF_8));
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_WRITEXML_FAILED, e);
		}
	}

	/**
	 * AES GCM 암호화가 적용된 XML 파일 저장 메소드
	 *
	 *
	 * @ param file
	 * @ param key
	 */
	public void writeBinaryAES128GCM(File file, byte[] key)
	{
		//파일 쓰기 설정
		try
		{
			file.setWritable(true);
			file.createNewFile();
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_WRITEXML_FAILED, e);
		}

		//저장 프로세스: XML 스트링을 바이트로 변경 -> 압축 -> 파일로 저장.
		try(ByteArrayOutputStream byteos = new ByteArrayOutputStream();
				DeflaterOutputStream deflateros = new DeflaterOutputStream(byteos);
				FileOutputStream fileos = new FileOutputStream(file))
		{
			//XML String 바이트화 및 압축
			deflateros.write(toByteArray(0));
			deflateros.finish();

			//암호화 및 파일 저장
			//원래 GCM의 nonce(CBC의 IV에 해당)는 random보다 겹칠 가능성을 줄이기 위해 순차 증가하는 것이 안전하나,
			//암호화하는 파일의 개수, 횟수가 적어 겹칠 가능성이 적으므로 편의상 랜덤 생성하여 활용함.
			byte[] nonce = new byte[12];
			new SecureRandom().nextBytes(nonce);
			fileos.write(nonce, 0, 12);
			GCMParameterSpec iv = new GCMParameterSpec(96, nonce);
			fileos.write(SgEncryption.getAES128Encode(key, byteos.toByteArray(), iv));
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_WRITEXML_FAILED, e);
		}
	}
	
	/**
	 * @ return  the doc
	 */
	public Document getDoc() 
	{
		return doc;
	}

	/**
	 * @ return  the rootNode
	 */
	public Node getRootNode()
	{
		return rootNode;
	}

	@Override
	public String toString()
	{
		StringWriter stringWriter = new StringWriter();
		try 
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(rootNode), new StreamResult(stringWriter));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return stringWriter.toString();
	}
	public byte[] toByteArray(int indent)
	{
		byte[] ret = null;
		try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream())
		{
			//XML XXE Attack 방지 feature 적용
			//2021-01-20 hjcho
			TransformerFactory tf = TransformerFactory.newInstance();
			
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer = tf.newTransformer();

			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			if(indent > 0)
			{
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			}
			else
			{
				transformer.setOutputProperty(OutputKeys.INDENT, "no");
			}
			transformer.transform(new DOMSource(rootNode), new StreamResult(byteStream));
			ret = byteStream.toByteArray();
		}
		catch(Exception e)
		{
//			LOGGER.debug(LOGMSG_WRITEXML_FAILED, e);
		}
		return ret;
	}
	/**
	 * 사용 가능한 Stream 인지 여부를 반환합니다.
	 * c# 압축 파일과 연동 시 구분을 위해 사용합니다.
	 * @ param stream
	 * @ return
	 */
	public boolean isAvailableStream(InputStream stream)
	{
		try 
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			builder.parse(stream);
			return true;
		}
		catch (Exception e) 
		{
			return false;
		}
	}
}
