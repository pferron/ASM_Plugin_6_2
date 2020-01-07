package com.axiomatics.plugin.attribute.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axiomatics.asm.admin.client.AsmAccessDenied_Exception;
import com.axiomatics.asm.admin.client.AsmAdminService;
import com.axiomatics.asm.admin.client.AsmClient;
import com.axiomatics.asm.admin.client.AsmWebServiceFault_Exception;
import com.axiomatics.asm.admin.client.TxAttribute;
import com.axiomatics.data.Attribute;
import com.axiomatics.data.AttributeDictionary;
import com.axiomatics.project.Project;
import com.axiomatics.project.ProjectInfo;

public class DownloadAttributeHandler62 extends AbstractHandler {
	
	static Logger logger = Logger.getLogger(UploadAttributeHandler62.class.getName());
	
	@Override
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
		
		String attributeFilePath 		= Project.getFilePath();
		if (attributeFilePath != null)
		{
			String projectFolder 					= Project.getProjectfolder();
			String workspacePath 					= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			Shell shell 							= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			boolean asmUploadStatus					= false;
			
			ProjectInfo asmInfo = new ProjectInfo();
	    	asmInfo.setprojectFolder(projectFolder);
	    	asmInfo.setShell(shell);
			
	       	Project.setupLogs(projectFolder, event, logger);
			
			logger.debug("Workspace Path: " + workspacePath);
			logger.debug("Shell: " + shell);
			logger.info("Project Folder: " + projectFolder);
			logger.info("Attribute file: " + attributeFilePath);
			
			// get ASM service
	    	AsmAdminService srv		= AsmClient.getService(projectFolder, asmInfo, event);
	
	    	// get ASM Project
	    	if (srv != null)
	    		asmUploadStatus = Project.getASMProject(projectFolder, asmInfo, event, srv);
	    	
	    	if (asmUploadStatus)  // check if the uploading status is still OKAY
        	{   	
		    	try {
		    		List<TxAttribute> xmlAttributeList = srv.getAttributeList(asmInfo.getCurrentProject());
					AttributeDictionary attributeListDico = new AttributeDictionary();
					List<Attribute> attributeList =  new ArrayList<>();
					
					for(int i=0;i<xmlAttributeList.size();i++)
					{
						Attribute dicoAttribute = new Attribute();
						dicoAttribute.setName(xmlAttributeList.get(i).getName());
						dicoAttribute.setNamespace(xmlAttributeList.get(i).getNamespace());
						dicoAttribute.setId(xmlAttributeList.get(i).getXacmlId());
						dicoAttribute.setCategory(xmlAttributeList.get(i).getCategory());
						dicoAttribute.setDatatype(xmlAttributeList.get(i).getXacmlDataType());
						dicoAttribute.setDescription(xmlAttributeList.get(i).getDescription());
						
						attributeList.add(dicoAttribute);
					}
					
					attributeListDico.setXmlns("http://www.axiomatics.com/config/acs/attributedictionary/v1");
					attributeListDico.setAttribute(attributeList);
				
					JAXBContext jaxbContext = JAXBContext.newInstance(AttributeDictionary.class);
					Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
					jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
					jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					jaxbMarshaller.marshal(attributeListDico, System.out);
			  		
	            	if (attributeList.size() > 0) // if there are XML attributes to save
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
							
							//logger.info("Total of all Attributes = " + attributeList.size());
			    	    	logger.info("XML Attributes");
			    	    	
			    			IWorkbenchWindow window = HandlerUtil
			    	  				.getActiveWorkbenchWindowChecked(event);
			    	  		MessageDialog.openInformation(window.getShell(),
			    	  				"Information",
			    	  				"XML attributes were downloaded"); 
			            }
	            	}
		    		
				} catch (AsmAccessDenied_Exception e) {
					logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
				} catch (AsmWebServiceFault_Exception e) {
					logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
				} catch (JAXBException e) {
					logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
				}
			} 
	  		
	    	srv.logout();  // disconnect from ASM
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
