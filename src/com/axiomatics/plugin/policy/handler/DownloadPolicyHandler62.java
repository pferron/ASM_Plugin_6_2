package com.axiomatics.plugin.policy.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import com.axiomatics.asm.admin.client.XmlDomain;
import com.axiomatics.plugin.attribute.handler.UploadAttributeHandler62;
import com.axiomatics.project.Project;
import com.axiomatics.project.ProjectInfo;

public class DownloadPolicyHandler62 extends AbstractHandler {
	
static Logger logger = Logger.getLogger(UploadAttributeHandler62.class.getName());
	
	@Override
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
		
		String policyFilePath 		= Project.getFilePath();
		if (policyFilePath != null)
		{
			String projectFolder 					= Project.getProjectfolder();
			String workspacePath 					= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
			Shell shell 							= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			boolean asmDownloadStatus				= false;
			
			ProjectInfo asmInfo = new ProjectInfo();
	    	asmInfo.setprojectFolder(projectFolder);
	    	asmInfo.setShell(shell);
			
			Project.setupLogs(projectFolder, event, logger);
			
			logger.debug("Workspace Path: " + workspacePath);
			logger.debug("Shell: " + shell);
			logger.info("Project Folder: " + projectFolder);
			
			// get ASM service
			logger.info("get ASM service");
	    	AsmAdminService srv		= AsmClient.getService(projectFolder, asmInfo, event);
	
	    	// get ASM Project
	    	logger.info("get ASM Project");
	    	if (srv != null)
	    		asmDownloadStatus = Project.getASMProject(projectFolder, asmInfo, event, srv);
	    	
	    	if (asmDownloadStatus)  // check if the download status is still OKAY
	    	{
	    		// get ASM Domain
	    		logger.info("get ASM Domain");
	    		asmDownloadStatus = Project.getExistingASMDomain(projectFolder, asmInfo, event, srv);
	    		
	    		if (asmDownloadStatus)  // check if the download status is still OKAY
	    		{   		
		    		// Retrieve the domain with the given name 
	    			logger.info("Retrieve the domain " + asmInfo.getDomainName() + " for the project " + asmInfo.getCurrentProject());
		        	XmlDomain domain;
					try {
						domain = srv.getDomainByName(asmInfo.getDomainName(), asmInfo.getCurrentProject()).get(0);
			        	// Read the main policy 
						logger.info("Read the main policy");
			        	byte[] mainPolicy = domain.getPdpConfiguration().getMainPolicy(); 
			        	// Read the ref policies. 
			        	logger.info("Read the ref policies");
			        	List<byte[]> refPolicies = domain.getPdpConfiguration().getReferenceablePolicies(); 
			        	// Prepare the zip structure 
			        	logger.info("Saving Policies ..");
		                FileDialog myPath = new FileDialog(shell, SWT.SAVE);
		                myPath.setFilterExtensions(new String [] {"*.zip"});
		                myPath.setFilterPath(projectFolder);
		                String strFilename = myPath.open();
		                if (strFilename != null) //if user enters a filename to save XML attributes
		                {
			                File file = new File(strFilename);
			                ZipOutputStream zos;
							try {
								zos = new ZipOutputStream(new FileOutputStream(file));
								// Write the root policy entry 
								logger.info("Write the root policy entry");
					        	ZipEntry rootPolicy = new ZipEntry("root-policy.xml"); 
					        	zos.putNextEntry(rootPolicy); 
					        	zos.write(mainPolicy); 
					        	zos.closeEntry(); 
					        	// Write the ref policy entries 
					        	logger.info("Write the ref policy entries");
					        	if (refPolicies!=null){ 
					        		for (byte[] refPolicyBytes: refPolicies){ 
					        			ZipEntry refPolicy = new ZipEntry("referenceable/"+UUID.randomUUID().toString()); 
					        			zos.putNextEntry(refPolicy); zos.write(refPolicyBytes); 
					        			zos.closeEntry(); } } 
					        	// Close the zip entry 
					        	logger.info("Close the zip file");
					        	zos.close(); 
					        	// Message : Policy zip file being downloaded
				                logger.info("Policy Package has being downloaded from ASM");
								IWorkbenchWindow window = HandlerUtil
				        				.getActiveWorkbenchWindowChecked(event);
				        		MessageDialog.openInformation(window.getShell(),
				        				"Information",
				        				"Policy Package has being downloaded from ASM"); 
							} catch (FileNotFoundException e) {
								logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
								e.printStackTrace();
							} catch (IOException e) {
								logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
								e.printStackTrace();
							}
			            }
					} catch (AsmAccessDenied_Exception e1) {
						logger.debug("Warning" + ExceptionUtils.getStackTrace(e1));
						e1.printStackTrace();
					} catch (AsmWebServiceFault_Exception e1) {
						logger.debug("Warning" + ExceptionUtils.getStackTrace(e1));
						e1.printStackTrace();
					} 
	    		}
	    	}    	
	    	logger.info("Disconnected from ASM");
	    	srv.logout();
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
