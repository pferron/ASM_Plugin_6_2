package com.axiomatics.plugin.converter.handler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xml.sax.SAXException;

import com.axiomatics.data.ALFAAttribute;
import com.axiomatics.data.ALFAFormat;
import com.axiomatics.data.XMLAttribute;
import com.axiomatics.parser.XMLParser;
import com.axiomatics.plugin.attribute.handler.UploadAttributeHandler62;
import com.axiomatics.project.Project;
import com.axiomatics.utilities.XACMLTranslation;

public class XMLtoALFA extends AbstractHandler{
		
		
	static Logger logger = Logger.getLogger(UploadAttributeHandler62.class.getName());
	
	private static final String XML_EXTENSION	= "xml";		
	
	@Override
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
		
	  XMLParser xmlParser 		= new XMLParser();
		
	  String attributeFilePath 	= Project.getFilePath();
	  
	  if (attributeFilePath != null)
		{
		  String projectFolder 						= Project.getProjectfolder();
		  String workspacePath 						= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		  Shell shell 								= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		  String extension 							= FilenameUtils.getExtension(attributeFilePath); // returns extension
	      List<XMLAttribute> xmlAttributeList 		= new ArrayList<>();
	      List<ALFAFormat> ALFAList					= new ArrayList<>();
			
	      Project.setupLogs(projectFolder, event, logger);
				
		  logger.debug("Workspace Path: " + workspacePath);
		  logger.info("Project Folder: " + projectFolder);
		  logger.info("Attribute file: " + attributeFilePath);
		  
		  if (extension.equalsIgnoreCase(XML_EXTENSION))
	      {
			                          // create a dialog with ok and cancel buttons and a question icon
	      	  logger.debug("create a dialog with ok and cancel buttons and a question icon");
	          MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES| SWT.NO);
	          dialog.setText("Confirmation");
	          dialog.setMessage("Please, confirm that you would like to convert " 
	        		  			+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1)
	        		  			+ " to an ALFA attribute file");
	          
	          if (dialog.open() == SWT.YES) // confirm the selected file is main policy
	          {	
	          	logger.info("Confirming the selected XML file is the wanted attribute file to be converted ...");
	          	          	
				try {
					logger.info("Parsing XML Attributes...");
					xmlParser.AttributeDictionaryParser(attributeFilePath, xmlAttributeList);
					
					logger.info("Total of XML Attributes = " + xmlAttributeList.size());
			      	IWorkbenchWindow window = HandlerUtil
			  				.getActiveWorkbenchWindowChecked(event);
			  		MessageDialog.openInformation(window.getShell(),
			  				"Information",
			  				xmlAttributeList.size()
			  				+ " XML Attribute(s) have being converted into ALFA Attribute(s) ");  
					
			  		if (xmlAttributeList.size() > 0) // if there are ALFA attributes to save
	            	{
			  			logger.info("Saving ALFA Attributes ..");
		                FileDialog myPath = new FileDialog(shell, SWT.SAVE);
		                myPath.setFilterExtensions(new String [] {"*.alfa"});
		                myPath.setFilterPath(projectFolder);
		                String strFilename = myPath.open();
		                if (strFilename != null) //if user enters a filename to save XML attributes
		                {
							BufferedWriter out = new BufferedWriter(new FileWriter(strFilename, false)); 
			
							logger.info("Writting ALFA Attributes into a file");
							groupAttributebyNameSpace(xmlAttributeList, ALFAList);
							
							for(int i=0;i<ALFAList.size();i++)
							{		
								// Writing a namespace into an ALFA file
								out.write("namespace " + ALFAList.get(i).getNamespace() + " {\n");
								for(int j=0;j<ALFAList.get(i).getAttribute().size();j++)
								{
									// writing attributes belonging to namespace into an ALFA file
									if (ALFAList.get(i).getAttribute().get(j).getXacmlDescription() != null 
											&& !ALFAList.get(i).getAttribute().get(j).getXacmlDescription().equalsIgnoreCase(""))
										out.write("\t/* " + ALFAList.get(i).getAttribute().get(j).getXacmlDescription() + "*/\n");
									//else
									//	out.write("\n");
									out.write("\t attribute " + ALFAList.get(i).getAttribute().get(j).getXacmlAttributeName() + " {\n");
									//out.write("\t attribute " + xmlAttributeList.get((i*1)+j).getName() + " {\n");
									
									//Translate XACML category and Type							
									ALFAList.get(i).getAttribute().get(j).setXacmlCategory(
											XACMLTranslation.reverseXacmlEquivalentCategory(ALFAList.get(i).getAttribute().get(j).getXacmlCategory()));
									ALFAList.get(i).getAttribute().get(j).setXacmlDataType(
											XACMLTranslation.reverseXacmlEquivalentType(ALFAList.get(i).getAttribute().get(j).getXacmlDataType()));
									
									out.write("\t\t category = " + ALFAList.get(i).getAttribute().get(j).getXacmlCategory() + "\n");
									out.write("\t\t id = \"" + ALFAList.get(i).getAttribute().get(j).getXacmlId() + "\"\n");
									out.write("\t\t type = " + ALFAList.get(i).getAttribute().get(j).getXacmlDataType() + "\n");
									out.write("\t}\n");
								}
								out.write("}\n\n");
							}
	
							out.close();
		                }
	            	}
	
					
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
	          }
			  
	      }
		  else
		  {
			  	logger.info("File being selected is not an XML attribute file");
		      	IWorkbenchWindow window = HandlerUtil
		  				.getActiveWorkbenchWindowChecked(event);
		  		MessageDialog.openInformation(window.getShell(),
		  				"Warning",
		  				"Incorrect : Please, select an XML attribute file to be converted into an ALFA Attribute File");   
		  }
		}
		else
		{
			// Message : select a correct source or destination
        logger.info("Incorrect : Please, select a correct source or destination");
			IWorkbenchWindow window;
			try {
				window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
				MessageDialog.openInformation(window.getShell(),
	    				"Warning",
	    				"Incorrect : Please, select a correct source or destination"); 
			} catch (ExecutionException e) {
				logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
				e.printStackTrace();
			}
		}    

	  LogManager.shutdown();
	  return event;
		
	}
	
	public List<ALFAFormat> groupAttributebyNameSpace(List<XMLAttribute> xmlAttributeList, List<ALFAFormat> ALFANameSpaceList) {
		
		//add first namespace + attribute
		ALFAFormat ALFANameSpace = new ALFAFormat();
		ALFANameSpace.setNamespace(xmlAttributeList.get(0).getNamespace());
		List<ALFAAttribute> ALFAAttributebyNameSpace = new ArrayList<>();
		
		ALFAAttribute ALFAAttribute = setALFAAttribute(xmlAttributeList, 0);
		ALFAAttributebyNameSpace.add(ALFAAttribute);
		
		ALFANameSpace.setAttribute(ALFAAttributebyNameSpace);
		ALFANameSpaceList.add(ALFANameSpace);
		
		
		for(int i=1;i<xmlAttributeList.size();i++)
		{
			for (int j=0;j<ALFANameSpaceList.size();j++)
			{
				// add attribute to existing namespace
				if (xmlAttributeList.get(i).getNamespace().equalsIgnoreCase(ALFANameSpaceList.get(j).getNamespace())) 
				{
					ALFAAttribute = setALFAAttribute(xmlAttributeList, i);
					ALFANameSpaceList.get(j).getAttribute().add(ALFAAttribute);
				}
				// add a new namespace + attribute
				else if (j == ALFANameSpaceList.size()-1) // 
				{
					ALFANameSpace = new ALFAFormat();
					ALFANameSpace.setNamespace(xmlAttributeList.get(i).getNamespace());
					ALFAAttributebyNameSpace = new ArrayList<>();
					
					ALFAAttribute = setALFAAttribute(xmlAttributeList, i);
					ALFAAttributebyNameSpace.add(ALFAAttribute);
					
					ALFANameSpace.setAttribute(ALFAAttributebyNameSpace);
					ALFANameSpaceList.add(ALFANameSpace);
					i++;
				}
			}
		}
		
		return ALFANameSpaceList;
		
	}
	
	public ALFAAttribute setALFAAttribute(List<XMLAttribute> xmlAttributeList, int index) {
		
		ALFAAttribute ALFAAttribute = new ALFAAttribute();
		ALFAAttribute.setXacmlAttributeName(xmlAttributeList.get(index).getName());
		ALFAAttribute.setXacmlCategory(xmlAttributeList.get(index).getCategory());
		ALFAAttribute.setXacmlDataType(xmlAttributeList.get(index).getDatatype());
		ALFAAttribute.setXacmlId(xmlAttributeList.get(index).getId());
		ALFAAttribute.setXacmllDescription(xmlAttributeList.get(index).getDescription());
		
		return ALFAAttribute;
		
	}
}
