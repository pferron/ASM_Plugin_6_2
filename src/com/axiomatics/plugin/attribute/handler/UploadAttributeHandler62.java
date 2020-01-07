package com.axiomatics.plugin.attribute.handler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xml.sax.SAXException;

import com.axiomatics.asm.admin.client.AsmAccessDenied_Exception;
import com.axiomatics.asm.admin.client.AsmAdminService;
import com.axiomatics.asm.admin.client.AsmClient;
import com.axiomatics.asm.admin.client.AsmWebServiceFault_Exception;
import com.axiomatics.data.ALFAAttribute;
import com.axiomatics.data.XMLAttribute;
import com.axiomatics.parser.ALFAParser;
import com.axiomatics.parser.XMLParser;
import com.axiomatics.project.Project;
import com.axiomatics.project.ProjectInfo;

public class UploadAttributeHandler62 extends AbstractHandler{
	
	static Logger logger = Logger.getLogger(UploadAttributeHandler62.class.getName());
	
	private static final String ALFA_EXTENSION	= "alfa";
	private static final String XML_EXTENSION	= "xml";
	
	@Override
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
		
		ALFAParser alfaParser 					= new ALFAParser();
		XMLParser xmlParser 					= new XMLParser();
		
		String attributeFilePath 		= Project.getFilePath();
		if (attributeFilePath != null)
		{
			String projectFolder 					= Project.getProjectfolder();
			String workspacePath 					= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();		
			Shell shell 							= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();			     
	        String extension 						= FilenameUtils.getExtension(attributeFilePath); // returns extension
	        boolean asmUploadStatus					= false;
	        List<XMLAttribute> xmlAttributeList 	= new ArrayList<>();
	        List<ALFAAttribute> alfaAttributeList	= new ArrayList<>();
	        
	        Project.setupLogs(projectFolder, event, logger);
			
			logger.debug("Workspace Path: " + workspacePath);
			logger.debug("Shell: " + shell);
			logger.info("Project Folder: " + projectFolder);
			logger.info("Attribute file: " + attributeFilePath);
			
		
	        if (extension.equalsIgnoreCase(ALFA_EXTENSION) || extension.equalsIgnoreCase(XML_EXTENSION))
	        {
	        	// create a dialog with ok and cancel buttons and a question icon
	        	logger.debug("create a dialog with ok and cancel buttons and a question icon");
	            MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES| SWT.NO);
	            dialog.setText("Confirmation");
	            dialog.setMessage("Please, confirm that you would like uploading attributes from " 
	            + attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1) );
	            
	            if (dialog.open() == SWT.YES) // confirm the selected file is main policy
	            {	
	            	logger.info("Confirming the selected file is the wanted attribute file to be uploaded ...");
	    	    	
	    	    	ProjectInfo asmInfo = new ProjectInfo();
	            	asmInfo.setprojectFolder(projectFolder);
	            	asmInfo.setShell(shell);
	            	
	            	// get ASM service
	            	AsmAdminService srv		= AsmClient.getService(projectFolder, asmInfo, event);
	
	            	// get ASM Projects
	            	if (srv != null)
	            		asmUploadStatus = Project.getASMProject(projectFolder, asmInfo, event, srv);
	            	
	            	if (asmUploadStatus)  // check if the uploading status is still OKAY
	            	{
	            		logger.info("Uploading Attributes...");
	            	 	ProgressMonitorDialog progress = new ProgressMonitorDialog(shell);
	            	 	     	 	
		                try {
							progress.run(true, true, new IRunnableWithProgress(){
							    public void run(IProgressMonitor monitor) throws InterruptedException {
							        //monitor.beginTask("In Progress ...", 100);
							    	monitor.beginTask("In Progress ...", IProgressMonitor.UNKNOWN);
							        // execute the task ...			    	
							    	
							    	try {
							    		if (extension.equalsIgnoreCase(ALFA_EXTENSION))
							    		{
							    			logger.info("Uploading ALFA Attributes...");
								    		String strFileContent = new String(Files.readAllBytes(Paths.get(attributeFilePath)));	
								    		alfaParser.ALFAAttributeParser(strFileContent, alfaAttributeList);
								    		if (alfaAttributeList.size() > 0)
								    			AsmClient.createALFAAttributeInDictionary(alfaAttributeList, asmInfo.getCurrentProject(), srv);
							    		} 
							    		else
							    		{
							    			logger.info("Uploading XML Attributes...");
							    			xmlParser.AttributeDictionaryParser(attributeFilePath, xmlAttributeList);
						    			 	if (xmlAttributeList.size() > 0)
						    			 		AsmClient.createXMLAttributeInDictionary(xmlAttributeList, asmInfo.getCurrentProject(), srv);
							    		}
							    			
							    		
									} catch (XPathExpressionException | SAXException | IOException
											| ParserConfigurationException e) {
										logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
										e.printStackTrace();
									} catch (AsmAccessDenied_Exception e) {
										logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
										e.printStackTrace();
									} catch (AsmWebServiceFault_Exception e) {
										logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
										e.printStackTrace();
									} 
												    	 				
									monitor.done();		        		 
						 
							    }											
							});
						} catch (InvocationTargetException e) {
							logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
							window(event, "Warning", ExceptionUtils.getStackTrace(e));      
							e.printStackTrace();
						} catch (InterruptedException e) {
							logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
							window(event, "Warning", ExceptionUtils.getStackTrace(e));
							e.printStackTrace();
						}	    
		                /********************************************************************************/ 
		               
		                //Inform user about uploading status
		                if (extension.equalsIgnoreCase(XML_EXTENSION))
		                {
			                if (xmlAttributeList.size() > 0)
			                {
			                	// Message : Attributes being uploaded
				                logger.info(xmlAttributeList.size() 
				                		+ " XML Attribute(s) have being uploaded into the ASM Dictionary for your " 
				                		+ asmInfo.getCurrentProject() 
				                		+ " project");
				                
								IWorkbenchWindow window = HandlerUtil
				        				.getActiveWorkbenchWindowChecked(event);
								
				        		MessageDialog.openInformation(window.getShell(),
				        				"Information",
				        				xmlAttributeList.size()
				        				+ " XML Attribute(s) have being uploaded into the ASM Dictionary for your " 
				        				+ asmInfo.getCurrentProject() + " project"); 
			                }
			                else
			                {
			                	// Message : Attributes not being uploaded
				                logger.info("No XML Attributes being loaded since no XML attributes were found in " 
				                		+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1));
				                
								IWorkbenchWindow window = HandlerUtil
				        				.getActiveWorkbenchWindowChecked(event);
								
				        		MessageDialog.openInformation(window.getShell(),
				        				"Information",
				        				"No XML Attributes being loaded since no XML attributes were found in " 
				        	                	+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1)); 
			                }
		                }
		                
		                if (extension.equalsIgnoreCase(ALFA_EXTENSION))
		                {
		                	 if (alfaAttributeList.size() > 0)
				                {
				                	// Message : Attributes being uploaded
					                logger.info(alfaAttributeList.size() 
					                		+ " ALFA Attribute(s) have being uploaded into the ASM Dictionary for your " 
					                		+ asmInfo.getCurrentProject() 
					                		+ " project");
					                
									IWorkbenchWindow window = HandlerUtil
					        				.getActiveWorkbenchWindowChecked(event);
									
					        		MessageDialog.openInformation(window.getShell(),
					        				"Information",
					        				alfaAttributeList.size()
					        				+ " ALFA Attribute(s) have being uploaded into the ASM Dictionary for your " 
					        				+ asmInfo.getCurrentProject() + " project"); 
				                }
				                else
				                {
				                	// Message : Attributes not being uploaded
					                logger.info("No ALFA Attributes being loaded since no ALFA attributes were found in " 
					                		+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1));
					                
									IWorkbenchWindow window = HandlerUtil
					        				.getActiveWorkbenchWindowChecked(event);
									
					        		MessageDialog.openInformation(window.getShell(),
					        				"Information",
					        				"No ALFA Attributes being loaded since no ALFA attributes were found in " 
					        	                	+ attributeFilePath.substring(attributeFilePath.lastIndexOf("/")+1)); 
				                }
		                }         		
	            	}
	            	
	            	srv.logout();   // disconnect from ASM
	            }
	        }
	        else
	    	{
	        	logger.info("File being selected is not a XML or ALFA attribute file");
	        	IWorkbenchWindow window = HandlerUtil
	    				.getActiveWorkbenchWindowChecked(event);
	    		MessageDialog.openInformation(window.getShell(),
	    				"Warning",
	    				"Incorrect : Please, select a XML or an ALFA attribute file to upload");   
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
	
	private IWorkbenchWindow window(ExecutionEvent event, String title, String message) throws ExecutionException
	{
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(),
				title,
				message); 
		
		
		return window;
	}

}
