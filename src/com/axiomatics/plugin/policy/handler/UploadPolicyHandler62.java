package com.axiomatics.plugin.policy.handler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axiomatics.asm.admin.client.AsmAdminService;
import com.axiomatics.asm.admin.client.AsmClient;
import com.axiomatics.project.Project;
import com.axiomatics.project.ProjectInfo;
import com.axiomatics.utilities.Packager;

public class UploadPolicyHandler62 extends AbstractHandler{
    
	private final static int PACKAGE_UPLOAD 	= 0;
	private final static int PACKAGE 			= 1;
	
	private static final String POLICY_PACKAGE_FILE = "/policy.zip";
	private static final String XML_EXTENSION = "xml";
	
	static Logger logger = Logger.getLogger(UploadPolicyHandler62.class.getName());
	
	@Override
    public Object execute(ExecutionEvent event) throws org.eclipse.core.commands.ExecutionException {
	    
		String mainPolicyFilePath 	= Project.getFilePath();
		if (mainPolicyFilePath != null)
		{
			String projectFolder 				= Project.getProjectfolder();
			String workspacePath 				= ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();		
			Shell shell 						= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();			  
	        String extension 					= FilenameUtils.getExtension(mainPolicyFilePath); // returns extension
	        ProjectInfo asmInfo 				= new ProjectInfo();
	        boolean asmUploadStatus				= false;
			
	        Project.setupLogs(projectFolder, event, logger);
			
			logger.debug("Workspace Path: " + workspacePath);
			logger.debug("Shell: " + shell);
			logger.info("Project Folder: " + projectFolder);
			logger.info("Main selected policy file: " + mainPolicyFilePath);
	        
	        
	        if (extension.equalsIgnoreCase(XML_EXTENSION))
	        {
	    		// create a dialog with ok and cancel buttons and a question icon
	        	logger.debug("create a dialog with ok and cancel buttons and a question icon");
	            MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES| SWT.NO);
	            dialog.setText("Confirmation");
	            dialog.setMessage("Please, confirm " + mainPolicyFilePath.substring(mainPolicyFilePath.lastIndexOf("/")+1) + " is your main policy file?");
	
	            if (dialog.open() == SWT.YES) // confirm the selected file is main policy
	            {	        
	            	logger.info("Confirming the selected file is the main policy...");
	                File mainPolicy = new File(mainPolicyFilePath);
	                String strPolicyFolder = mainPolicyFilePath.substring(0, mainPolicyFilePath.lastIndexOf("/"));
	                File policyFolder = new File(strPolicyFolder);
	                
	                MessageDialog multipleChoice = new MessageDialog(shell, "Options", null,
	    		    "Please, choose your option", MessageDialog.INFORMATION, new String[] { "Package and Upload",
	    		    "Package", "Cancel" }, 0);
	    	
	        
	            	int choice = multipleChoice.open();
	            	if ( choice == PACKAGE_UPLOAD || choice == PACKAGE)
	            	{
	            		String policyPackageFile = new String(projectFolder + POLICY_PACKAGE_FILE);
		                
		                if (choice == PACKAGE)
		                {
		                	logger.info("Packaging policies ..");
			                FileDialog myPath = new FileDialog(shell, SWT.SAVE);
			                myPath.setFilterExtensions(new String [] {"*.zip"});
			                myPath.setFilterPath(projectFolder);
			                String result = myPath.open();
			                
			                policyPackageFile = result;
		                }
		                
		                if (mainPolicy.exists()==false){
		                    logger.info(mainPolicy.getAbsolutePath()+" does not exist.");
		                    System.exit(0);
		                }        
		                
		                if (policyFolder.exists()==false || policyFolder.isDirectory()==false){
		                    logger.info(policyFolder.getAbsolutePath()+" does not exist or is not a folder.");
		                }
		                Packager p = new Packager(mainPolicy, policyFolder);
		                try {
		                    File policyPackage = p.producePackage(policyPackageFile);
		                    logger.info("Package written to "+ policyPackage.getAbsolutePath());
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		                
		                if (choice == PACKAGE_UPLOAD)
		                {
		                	logger.info("Uploading policy package ..");
		                	asmInfo.setPolicyPackageFile(policyPackageFile);
		                	asmInfo.setprojectFolder(projectFolder);
		                	asmInfo.setShell(shell);
	
		                	// get ASM service
		                	AsmAdminService srv		= AsmClient.getService(projectFolder, asmInfo, event);
	
		                	// get Projects List
		                	if (srv != null)
		                		asmUploadStatus = Project.getASMProject(projectFolder, asmInfo, event, srv);
		                					                
		                	if (asmUploadStatus)  // check if the uploading status is still OKAY
		                	{
		                		
		                		asmUploadStatus = Project.getASMDomain(projectFolder, asmInfo, event, srv);
		                		
		                		if (asmUploadStatus)  // check if the uploading status is still OKAY
		                		{
		                			logger.info("Loading policy package...");
			                	 	ProgressMonitorDialog progress = new ProgressMonitorDialog(shell);
			                	 	
					                try {
										progress.run(true, true, new IRunnableWithProgress(){
										    public void run(IProgressMonitor monitor) throws InterruptedException {
										        //monitor.beginTask("In Progress ...", 100);
										    	monitor.beginTask("In Progress ...", IProgressMonitor.UNKNOWN);
										        // execute the task ...
			                		
								    try {
								    	AsmClient.sendPolicyPackage(asmInfo, event);
						        		
									} catch (Exception e) {
										e.printStackTrace();
									}	
									
									
									/********************************************************************************/
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
					                
					                // Message : XACML file being uploaded
					                logger.info("Policy Package has being uploaded into ASM");
									IWorkbenchWindow window = HandlerUtil
					        				.getActiveWorkbenchWindowChecked(event);
					        		MessageDialog.openInformation(window.getShell(),
					        				"Information",
					        				"Your Policy Package has being uploaded into ASM"); 
					        		logger.info("Disconnected from ASM");
					        		srv.logout();
		                		}
		                	}
		                }			                
		            }
	        	}  // end of checking if selected policy file is main policy file
	        }
	        else
	        {
	        	logger.info("File being selected is not a XML file");
	        	IWorkbenchWindow window = HandlerUtil
	    				.getActiveWorkbenchWindowChecked(event);
	    		MessageDialog.openInformation(window.getShell(),
	    				"Warning",
	    				"Incorrect : Please, select a XACML file to upload");   
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
