package com.axiomatics.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

import com.axiomatics.data.XMLAttribute;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


public class XMLParser {
	
	private static final String ATTRIBUTE_NAME 	= "Name";
    private static final String NAMESPACE 		= "Namespace";
    private static final String DATATYPE 		= "Datatype";
    private static final String CATEGORY 		= "Category";
    private static final String DESCRIPTION 	= "Description";
    
    
    static Logger logger = Logger.getLogger(XMLParser.class.getName());
    		
    public void AttributeDictionaryParser(String filename, List<XMLAttribute> attributeList) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException 
	{				
	    FileInputStream fileIS = new FileInputStream(filename);
	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = builderFactory.newDocumentBuilder();
	    Document xmlDocument = builder.parse(fileIS);
	    XPath xPath = XPathFactory.newInstance().newXPath();
	    String expression = "/AttributeDictionary/Attribute";
	    NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
	    logger.debug("-------------------------------------");
	    logger.debug("Nb of Nodes: " + nodeList.getLength());
	    logger.debug("-------------------------------------");
	    
	    for(int i=0;i<nodeList.getLength();i++)
		{
	    	XMLAttribute attribute = new XMLAttribute();
	    	
	    	String id = xPath.evaluate("Id", nodeList.item(i));
	    	attribute.setId(id);
	    	logger.debug("Id: " + id);
			
			String attributeName = xPath.evaluate(ATTRIBUTE_NAME, nodeList.item(i));
			attribute.setName(attributeName);
			logger.debug(ATTRIBUTE_NAME + ": " + attributeName);
			
			String nameSpace = xPath.evaluate(NAMESPACE, nodeList.item(i));
			attribute.setNamespace(nameSpace);
			logger.debug(NAMESPACE + ": " + nameSpace);	
			
			String dataType = xPath.evaluate(DATATYPE, nodeList.item(i));
			attribute.setDatatype(dataType);
			logger.debug(DATATYPE + ": " + dataType);
			
			String category = xPath.evaluate(CATEGORY, nodeList.item(i));
			attribute.setCategory(category);
			logger.debug(CATEGORY + ": " + category);
			
			String description = xPath.evaluate(DESCRIPTION, nodeList.item(i));
			attribute.setDescription(description);
			logger.debug(DESCRIPTION + ": " + description);
			
			attributeList.add(attribute);						
		}
	    
	    logger.info("-------------------------------------");
	    logger.info("    Completed Attribute Parsing!!!");
	    logger.info("-------------------------------------");
	}
}
