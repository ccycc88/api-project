package com.api.project.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.StAXStreamBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;
import org.jdom2.xpath.XPathBuilder;

public class XmlUtil {

	/**
	 * 获得JDOM对象
	 * 
	 * @param fileName
	 *            文件路径
	 * @return
	 * @throws Exception
	 */
	public static Document getDocument(String fileName) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document doc = null;
		File file = new File(fileName);
		doc = builder.build(file);

		return doc;
	}

	/**
	 * 获得JDOM对象 @param in @return @throws Exception @throws
	 */
	public static Document getDocument(InputStream in) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		doc = builder.build(in);

		return doc;
	}

	/**
	 * 获得JDOM对象
	 * 
	 * @param reader
	 * @return
	 */
	public static Document getDocument(Reader reader) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		doc = builder.build(reader);
		return doc;
	}

	/**
	 * 用Stax获取JODM对象
	 * 
	 * @param streamReader
	 * @return
	 * @throws Exception
	 */
	public static Document getDocumentByStax(XMLStreamReader streamReader) throws Exception {
		StAXStreamBuilder builder = new StAXStreamBuilder();
		Document doc = null;
		doc = builder.build(streamReader);

		return doc;
	}

	/**
	 * 用Stax获取JODM对象
	 * 
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static Document getDocumentByStax(Reader reader) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;

		doc = builder.build(reader);

		return doc;
	}

	/**
	 * 用Stax获取JODM对象
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static Document getDocumentByStax(InputStream in) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		doc = builder.build(in);

		return doc;
	}

	/**
	 * 依据xpath查找第一个Element的value
	 * 
	 * @param doc
	 *            JODM对象
	 * @param xpath
	 * @return
	 */
	public static String getValueByXpath(Object doc, String xpath) {
		String value = null;
		try {
			
			Object node = XPath.selectSingleNode(doc, xpath);
			if (node instanceof Element) {
				Element element = (Element) node;
				value = element.getValue();
			} else if (node instanceof Attribute) {
				Attribute attribute = (Attribute) node;
				value = attribute.getValue();
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 依据xpath查找第一个Element的value
	 * 
	 * @param msg
	 *            xml字符串
	 * @param xpath
	 * @return
	 * @throws Exception
	 */
	public static String getValueByXpath(String msg, String xpath) throws Exception {
		StringReader reader = new StringReader(msg);
		Document doc = getDocument(reader);
		return getValueByXpath(doc, xpath);
	}

	/**
	 * 依据xpath查找满足条件的Element
	 * 
	 * @param context
	 * @param xpath
	 * @return
	 */
	public static List<Element> findElement(Object context, String xpath) {
		try {
			List<Element> nodes = (List<Element>) XPath.selectNodes(context, xpath);
			return nodes;
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 依据xpath查找满足条件的第一个Element
	 * 
	 * @param context
	 * @param xpath
	 * @return
	 */
	public static Element findFirstElement(Object context, String xpath) {
		try {
			Element node = (Element) XPath.selectSingleNode(context, xpath);
			return node;
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * jdom转换为xmlString
	 */
	public static String asXml(Document doc) {
		try {
			Format format = Format.getPrettyFormat();
			format.setEncoding("UTF-8");// 设置xml文件的字符为gbk，解决中文问题
			XMLOutputter xmlout = new XMLOutputter(format);
			// ByteArrayOutputStream bo = new ByteArrayOutputStream();
			StringWriter writer = new StringWriter();
			xmlout.output(doc, writer);
			String xmlStr = writer.toString();
			return xmlStr;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Attribute字段特殊处理
	 */
	public static String convertXMLKeyWord(String str) {
		int len = str.length();
		StringBuffer buf = new StringBuffer();
		String replaceMent = null;
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);

			switch (c) {
			case '&':
				replaceMent = "&amp;";
				break;
			case '<':
				replaceMent = "&lt;";
				break;
			case '>':
				replaceMent = "&gt;";
				break;
			case '"':
				replaceMent = "&quot;";
				break;
			case '\'':
				replaceMent = "&apos;";
			}
			if (replaceMent != null) {
				buf.append(replaceMent);
				replaceMent = null;
			} else {
				buf.append(c);
			}

		}

		return buf.toString();
	}
}
