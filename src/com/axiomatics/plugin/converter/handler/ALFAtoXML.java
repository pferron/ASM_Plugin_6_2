package com.axiomatics.plugin.converter.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.axiomatics.data.Attribute;
import com.axiomatics.data.AttributeDictionary;
import com.axiomatics.parser.ALFAParser;
import com.axiomatics.plugin.attribute.handler.UploadAttributeHandler62;
import com.axiomatics.project.Project;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class ALFAtoXML extends AbstractHandler{
		
		
	static Logger logger = Logger.getLogger(UploadAttributeHandler62.class.getName());
	
	private static final String ALFA_EXTENSION	= "alfa";
		
	@Override
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
		
	  ALFAParser alfaParser 	= new ALFAParser();
		
	  String attributeFilePath 	= Project.getFilePath();
	  
	  if (attributeFilePath != null)
		{
		  String projectFolder 							= Project.getProjectfolder();
		  String workspacePath 							= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		  Shell shell 									= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		  String extension 								= FilenameUtils.getExtension(attributeFilePath); // returns extension
	      List<ALFAAttribute> alfaAttributeList			= new ArrayList<>();
	      List<ALFAAttribute> UniqueAlfaAttributeList	= new ArrayList<>();
			
	      Project.setupLogs(projectFolder, event, logger);
				
		  logger.debug("Workspace Path: " + workspacePath);
		  logger.info("Project Folder: " + projectFolder);
		  logger.info("Attribute file: " + attributeFilePath);
		  
		  if (extension.equalsIgnoreCase(ALFA_EXTENSION))
	      {
	      	  // create a dialog with ok and cancel buttons and a question icon
	      	  logger.debug("create a dialog with ok and cancel buttons and a question icon");
	          MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES| SWT.NO);
	          dialog.setText("Confirmation");
	          dialog.setMessage("Please, confirm that you would like to convert " 
	        		  			+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1)
	        		  			+ " to an XML attribute file");
	          
	          if (dialog.open() == SWT.YES) // confirm the selected file is main policy
	          {	
	          	logger.info("Confirming the selected ALFA file is the wanted attribute file to be converted ...");
	          	String strFileContent;
				try {
					logger.info("Parsing ALFA Attributes...");
					strFileContent = new String(Files.readAllBytes(Paths.get(attributeFilePath)));
					alfaParser.ALFAAttributeParser(strFileContent, alfaAttributeList);
					
					// remove duplicated attributes (keep only attributes with unique attribute id + type +  )
					for(int i=0;i<alfaAttributeList.size();i++)
					{
						if (i == 0)
							UniqueAlfaAttributeList.add(alfaAttributeList.get(0));
	
						for(int j=0;j<UniqueAlfaAttributeList.size();j++)
						{
							 if (alfaAttributeList.get(i).getXacmlId().equalsIgnoreCase(UniqueAlfaAttributeList.get(j).getXacmlId())
									 && alfaAttributeList.get(i).getXacmlCategory().equalsIgnoreCase(UniqueAlfaAttributeList.get(j).getXacmlCategory())
									 && alfaAttributeList.get(i).getXacmlDataType().equalsIgnoreCase(UniqueAlfaAttributeList.get(j).getXacmlDataType()))
								 break;
							 if (j == UniqueAlfaAttributeList.size() -1)
								 UniqueAlfaAttributeList.add(alfaAttributeList.get(i));		 
						}									
					}
					
					AttributeDictionary attributeListDico = new AttributeDictionary();
					List<Attribute> attributeList =  new ArrayList<>();
					
					for(int i=0;i<UniqueAlfaAttributeList.size();i++)
					{
						Attribute dicoAttribute = new Attribute();
						dicoAttribute.setName(UniqueAlfaAttributeList.get(i).getXacmlAttributeName());
						dicoAttribute.setNamespace(UniqueAlfaAttributeList.get(i).getXacmlNameSpace());
						dicoAttribute.setId(UniqueAlfaAttributeList.get(i).getXacmlId());
						dicoAttribute.setCategory(UniqueAlfaAttributeList.get(i).getXacmlCategory());
						dicoAttribute.setDatatype(UniqueAlfaAttributeList.get(i).getXacmlDataType());
						
						// Include description in the XML file even if the description is missing in the ALFA file
						if (UniqueAlfaAttributeList.get(i).getXacmlDescription() != null )
							dicoAttribute.setDescription(UniqueAlfaAttributeList.get(i).getXacmlDescription());
						else
							dicoAttribute.setDescription("");
						
						attributeList.add(dicoAttribute);
					}
					
					attributeListDico.setXmlns("http://www.axiomatics.com/config/acs/attributedictionary/v1");
					attributeListDico.setAttribute(attributeList);
				
					JAXBContext jaxbContext = JAXBContext.newInstance(AttributeDictionary.class);
					Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
					jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
					jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					jaxbMarshaller.marshal(attributeListDico, System.out);
					
					logger.info("Total of all Attributes = " + alfaAttributeList.size());
					logger.info("Total of Uniques Attributes = " + UniqueAlfaAttributeList.size());
					
			      	IWorkbenchWindow window = HandlerUtil
			  				.getActiveWorkbenchWindowChecked(event);
			  		MessageDialog.openInformation(window.getShell(),
			  				"Information",
			  				alfaAttributeList.size() + " attribute(s) have being found in " 
			  				+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1) + "\n"
			  				+ (alfaAttributeList.size() - UniqueAlfaAttributeList.size()) + " duplicated attributes\n"
			  				+ UniqueAlfaAttributeList.size() + " unique attribute(s) will be converted");  
			  		
	            	if (UniqueAlfaAttributeList.size() > 0) // if there are XML attributes to save
	            	{
				  		logger.info("Saving XML Attributes ..");
		                FileDialog myPath = new FileDialog(shell, SWT.SAVE);
		                myPath.setFilterExtensions(new String [] {"*.xml"});
		                myPath.setFilterPath(projectFolder);
		                String strFilename = myPath.open();
		                if (strFilename != null) //if user enters a filename to save XML attributes
		                {
			                File file = new File(strFilename);
							jaxbMarshaller.marshal(attributeListDico, file);
			            }
	            	}
	                
				} catch (IOException e) {
					e.printStackTrace();
					logger.debug(" Stacktrace => " + e);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
					logger.debug(" Stacktrace => " + e);
				} catch (SAXException e) {
					e.printStackTrace();
					logger.debug(" Stacktrace => " + e);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
					logger.debug(" Stacktrace => " + e);
				} catch (JAXBException e) {
					e.printStackTrace();
					logger.debug(" Stacktrace => " + e);
				}	    		
	          }		  
	      }
		  else
		  {
			  	logger.info("File being selected is not an ALFA attribute file");
		      	IWorkbenchWindow window = HandlerUtil
		  				.getActiveWorkbenchWindowChecked(event);
		  		MessageDialog.openInformation(window.getShell(),
		  				"Warning",
		  				"Incorrect : Please, select an ALFA attribute file to be converted into a XML Attribute File");   
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

}
