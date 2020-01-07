package com.axiomatics.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.axiomatics.data.ALFAAttribute;
import com.axiomatics.utilities.XACMLTranslation;

public class ALFAParser {
	
	private static final String POLICY 								= "policy";
	private static final String POLICYSET 							= "policyset";
	private static final String NAMESPACE 							= "namespace";
	private static final String ATTRIBUTE 							= "attribute";
    private static final String ID		 							= "id=";
    private static final String CATEGORY 							= "category=";
    private static final String TYPE 								= "type=";
    private static final String NAMESPACE_OPEN_CURLY_BRAKET 		= "{namespace";
	private static final String ATTRIBUTE_OPEN_CURLY_BRAKET 		= "{attribute"; 
	private static final String OBLIGATION							= "obligation";
	private static final String ADVICE								= "advice";
	
    
	static int nameSpaceCount 										= 0;
	static Logger logger 											= Logger.getLogger(ALFAParser.class.getName());
	
	private ALFAAttribute 		aLFAAttribute;
	
    
    public void ALFAAttributeParser(String str, List<ALFAAttribute> attributeList) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException 
	{	 	
    	String nameSpace			= "";
    	String strAttributeComments = "";
    	String description			= "";
    	String descriptionAttribute = "";
    	String comment				= "";
    	List<String> commentsList 	= new ArrayList<String>();
    	
    	/* Removing break lines and tabs */
	    str = str.replaceAll("\\r\\n|\\r|\\n|\\t", "");
	    
	    /* Remove policy set */
	    str = removeElement(str, POLICYSET);
	    logger.debug("File String => " + str);
	    
	    /* Remove policy */
	    str = removeElement(str, POLICY);
	    logger.debug("File String => " + str); 
	    
	    /*Remove Obligations*/
	    if (str.indexOf(OBLIGATION) != -1)
	    {
	    	str = removeObligationsOrAdvice(OBLIGATION, str);
	    	logger.debug("File String without obligation => " + str); 
	    }
	    
	    /*Remove Advice*/
	    if (str.indexOf(ADVICE) != -1)
	    {
	    	str = removeObligationsOrAdvice(ADVICE, str);
	    	logger.debug("File String without advice => " + str); 
	    }
	    
	    /* Store comments */
	    Pattern patternComment = Pattern.compile("/\\*(.*?)\\*/"); 
	    Matcher matcherComment = patternComment.matcher(str);
	   
	    while (matcherComment.find())
	    {
	    	comment = matcherComment.group().replaceAll("\\/", "").replaceAll("\\*", "").trim();
	    	logger.debug("Stored Comment => " + comment);
	    	commentsList.add(comment);
	    }
	    		
	    /* Removing space */
	    str = str.replaceAll(" ", "");
	    
	    /*Remove comments that are not for attributes */
	    str = removeComments(str);
	    
	    /* Removing importing aLFAAttribute lines */
	    Pattern patternImport = Pattern.compile("import(.*?).\\*"); 
	    Matcher matcherImport = patternImport.matcher(str);
	    while (matcherImport.find()) 
	    {
	    	String imported = matcherImport.group();
	    	// exclude automatically_imported for namespace
	    	if (!imported.substring(0,"imported".length()).equalsIgnoreCase("imported")) 
	    		str = str.replaceAll("import(.*?).\\*",""); 
	    }
    		    
	    /* store attribute comments*/
    	strAttributeComments = str;
	    logger.debug("Attribute comments => " + strAttributeComments);
	    
	    /* Removing all comments */
	    str = str.replaceAll("/\\*(.*?)\\*/","");
	    logger.debug("File String => " + str);  
	    
	    /* Attribute Pattern */
	    Pattern pattern = Pattern.compile(ATTRIBUTE + "(.+?)}");
	    Matcher matcher = pattern.matcher(str);
	    // Find all matches
	    while (matcher.find()) {
	    	
	    	//Get the namespace hierarchy
	    	nameSpace = str.substring(0, str.indexOf(ATTRIBUTE_OPEN_CURLY_BRAKET));
	    	nameSpace = getNameSpace(nameSpace);
	    	logger.debug("NameSpace => " + nameSpace);
	    	
	    	// Get the Attribute
	    	String attribute = matcher.group();
	    	logger.debug("Attribute => " + attribute);	
	    	
	    	// Get the attribute description 
	    	Pattern patternDescription = Pattern.compile("/\\*(.*?)\\*/" 
	    			+ attribute.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}"));
		    Matcher matcherDescription = patternDescription.matcher(strAttributeComments);
		   
		    descriptionAttribute = "";
		    if (matcherDescription.find())
		    {
		    	description = matcherDescription.group();
		    	description = description.substring(0, description.indexOf(attribute));
		    	logger.debug("Description => " + description);
		    	
		    	// Remove the attribute description from the content file
		    	description = description.replaceAll("\\/", "\\\\/").replaceAll("\\*", "\\\\*")
		    			.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
		    			.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
		    	strAttributeComments = strAttributeComments.replaceFirst(description, "");
		    	logger.debug("Attribute Comments => " + strAttributeComments);
		    	
		    	description = description.replaceAll("\\\\/", "").replaceAll("\\\\*", "").replaceAll("\\*", "");
		    	
		    	for (String commentDescription : commentsList) 
		    	{
		    		if (description.equalsIgnoreCase(commentDescription.replaceAll(" ", "")))
		    		{
		    			descriptionAttribute = commentDescription;
		    			break;
		    		}
		    		else
		    			descriptionAttribute = "";
		    	}
		    	
		    	logger.debug("Description => " + descriptionAttribute);
		    	
		    }		    
    	
		    // Extract attribute elements
	    	extractAttrElements(nameSpace, descriptionAttribute, attribute.replaceAll(ATTRIBUTE, ""), attributeList);
	    	
	    	// Remove the Attribute from the content file
	    	attribute = attribute.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
	    	str = str.replaceFirst(attribute, "");
    		logger.debug("File String => " + str);
    		
	    	while (str.contains("{}")) 
	    	{
	    		String strPart1 = str.substring(0, str.indexOf("{}"));
	    		strPart1 = strPart1.substring(0, strPart1.lastIndexOf(NAMESPACE));
	    		logger.debug("File String Part1 => " + strPart1);
	    		String strPart2 = str.substring(str.indexOf("{}") + 2);
	    		logger.debug("File String Part2 => " + strPart2);	 
	    		str = strPart1 + strPart2;
	    		logger.debug("File String => " + str);
	    	}
	    }    	
	}
    
    private String getNameSpace(String nameSpacePath)
    {
    	nameSpacePath = nameSpacePath.replaceAll("\\" + NAMESPACE_OPEN_CURLY_BRAKET, ".");
    	nameSpacePath = nameSpacePath.replaceAll(NAMESPACE, "");
    	return nameSpacePath;
    }
    
    
    private void extractAttrElements(String namespace, String description, String str, List<ALFAAttribute> attributeList )
    {
    	String alfaAttributeName	= null;
    	String alfaCategory			= null;
    	String alfaId				= null;
    	String alfaType				= null;
    	String[] element			= new String[4];
    	int categoryPos				= 0;
    	int	idPos					= 0;
    	int typePos					= 0;
    	aLFAAttribute						= new ALFAAttribute();
    	
    	Map<Integer, String> AttrElementsOrder = new TreeMap<Integer, String>();
    	
    	alfaAttributeName = str.substring(0, str.indexOf("{"));
       	aLFAAttribute.setXacmlAttributeName(alfaAttributeName);
    	aLFAAttribute.setXacmlNameSpace(namespace);
    	aLFAAttribute.setXacmllDescription(description);
    	logger.debug("Attribute Name = " + alfaAttributeName);

    	str = str.substring(str.indexOf("{",2));
   	
    	/* Determine Category, id, and type order */
    	categoryPos = str.indexOf(CATEGORY);
    	AttrElementsOrder.put(categoryPos, CATEGORY);
    	idPos 		= str.indexOf("id=");
    	AttrElementsOrder.put(idPos, ID);
    	typePos		= str.indexOf(TYPE);
    	AttrElementsOrder.put(typePos, TYPE);
    	
    	Object firstElement = AttrElementsOrder.keySet().toArray()[0];
    	element[0] = AttrElementsOrder.get(firstElement);
    	
    	Object secondElement = AttrElementsOrder.keySet().toArray()[1];
    	element[1] = AttrElementsOrder.get(secondElement);
    	
    	Object thirdElement = AttrElementsOrder.keySet().toArray()[2];
    	element[2] = AttrElementsOrder.get(thirdElement);
    	
    	element[3] = "}";
    	
    	for(int i=0;i<3;i++)
    	{
	    	Pattern pattern = Pattern.compile(element[i] + "(.+?)" + element[i+1]);
		    Matcher matcher = pattern.matcher(str);
		    // Find all matches
		    while (matcher.find()) {
		    	switch(element[i])
		    	{
		    	   case CATEGORY :
		    		   alfaCategory = matcher.group().replaceAll(element[i], "").replaceAll(element[i+1], "");
		    		   aLFAAttribute.setXacmlCategory(XACMLTranslation.XacmlEquivalentCategory(alfaCategory));
		    		   logger.debug(CATEGORY + XACMLTranslation.XacmlEquivalentCategory(alfaCategory));
		    	      break; 	    	   
		    	   case TYPE :
		    		   alfaType = matcher.group().replaceAll(element[i], "").replaceAll(element[i+1], "");
		    		   aLFAAttribute.setXacmlDataType(XACMLTranslation.XacmlEquivalentType(alfaType));
		    		   logger.debug(TYPE + XACMLTranslation.XacmlEquivalentType(alfaType));
		    	      break; 	    
		    	   default : 
		    		   alfaId = matcher.group().replaceAll(element[i], "").replaceAll(element[i+1], "");
		    		   aLFAAttribute.setXacmlId(alfaId.replaceAll("\"", ""));
		    		   logger.debug(ID + alfaId.replaceAll("\"", ""));
		    	}
		    }
    	}
    	
    	attributeList.add(aLFAAttribute);
    }
    
    private String removeObligationsOrAdvice(String obligationsOrAdvice, String str)
    {
    	 while (str.indexOf(obligationsOrAdvice) != -1)
 	    {
 	    	int startPos = str.indexOf(obligationsOrAdvice) + obligationsOrAdvice.length();
 	    	int quoteCounter = 0;
 	    	int inc = 0;
 	    	
 	    	while (quoteCounter < 2)
 	    	{
 	    		inc++;
 	    		if (str.substring(startPos + inc, startPos + inc + 1).equalsIgnoreCase("\""))
 	    			quoteCounter++;	    		
 	    	}
 	    	
 	    	String obligation = str.substring(startPos - obligationsOrAdvice.length(), startPos + inc + 1);
 	    	str = str.replaceFirst(obligation, "");
 	    }
    	 
    	return str;
    }
    
    private String removeComments(String str)
    {
    	String 	strTmp 			= str;
	    String 	strComment 		= "";
	    int		startPos		= 0;
	    int 	endPos			= 0;	
	    int 	lengthComment	= 0;	
	    
	    while ((startPos < strTmp.length()) && (strTmp.substring(startPos, strTmp.length()).indexOf("/*") != -1))
	    {
		    startPos = strTmp.substring(0, strTmp.length()).indexOf("/*");
		    
		    lengthComment = strTmp.substring(startPos + 2, strTmp.length()).indexOf("*/");
		    
		    endPos = startPos + lengthComment + 4;
		    
		    strComment 	= strTmp.substring(startPos, endPos);
	    	logger.debug("strComment => " + strComment);
		   
		    if (!strTmp.substring(endPos, endPos + ATTRIBUTE.length())
		    		.equalsIgnoreCase(ATTRIBUTE))
		    {
		    	strComment 	= strTmp.substring(startPos, endPos).replaceAll("\\*", "\\\\*")
		    			.replaceAll("\\+", "\\\\+").replaceAll("\\-", "\\\\-");
		    	strTmp 		= strTmp.replaceAll(strComment, "");
		    	str 		= str.replaceAll(strComment, "");
		    }
		    else
		    {
		    	startPos = endPos;
		    	strTmp = strTmp.substring(startPos, strTmp.length());
		    }
	    }
	    
	    return str;
    }
    
    
    private String removeElement(String str, String strElement)
    {
    	while (str.indexOf(strElement) != -1)
	    {
	    	int startPos = str.indexOf(strElement);
	    	boolean bfoundOpenCurlyBracket = false;
	    	int inc = 0;
	    	int counter = 0;
	    	String element = "";
	    	
	    	do
	    	{
	    		
	    		if (str.substring(startPos + inc , startPos + inc + 1 ).equalsIgnoreCase("{"))
	    		{
	    			bfoundOpenCurlyBracket = true;
	    			counter++;
	    		}
	    			
	    		if (str.substring(startPos+ inc , startPos + inc + 1 ).equalsIgnoreCase("}"))
	    			counter--;
	    		
	    		inc++;
	    	}
	    	while ((bfoundOpenCurlyBracket == false) || (counter != 0));
	    	
	    	// Remove the Element from the content file
	    	element = str.substring(startPos, startPos + inc);
	    	element = element.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}")
	    			.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
	    			.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")
	    			.replaceAll("\\*", "\\\\*");
	    	str = str.replaceFirst(element, "");
	    }
    	
    	return str;

    }

}

