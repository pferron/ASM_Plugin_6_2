package com.axiomatics.project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import com.axiomatics.asm.admin.client.AsmAccessDenied_Exception;
import com.axiomatics.asm.admin.client.AsmAdminService;
import com.axiomatics.asm.admin.client.AsmWebServiceFault_Exception;
import com.axiomatics.asm.admin.client.XmlDomain;
import com.axiomatics.plugin.policy.handler.MyTitleAreaDialog;

public class Project {
	
	private static final String ASM_PROPERTIES_FILE = "/asm.properties";
	private static final String LOG_PROPERTIES_FILE = "/log4j.properties";
	
	private final static int OKAY				= 0;	
	
	private final static int NEW_DOMAIN 		= 0;
	private final static int CANCEL_DOMAIN	 	= -1;
	
	public static String projectFolder = "";
	
	static Logger logger = Logger.getLogger(Project.class.getName());
	
	
	public static boolean getASMDomain(String path, ProjectInfo asmInfo, ExecutionEvent event, AsmAdminService srv) throws ExecutionException
	{
		boolean asmUploadStatus = false;
		
		MessageDialog domainOption = new MessageDialog(asmInfo.getShell(), "Domain Options", null,
     		    "Attach Policy Package to", MessageDialog.INFORMATION, new String[] { "New Domain",
     		    "Existing Domains"}, 0);
		
		int domainChoice = domainOption.open();
		if (domainChoice != CANCEL_DOMAIN)
		{
    		if (domainChoice == NEW_DOMAIN)
    		{			                					
    			asmInfo.setNewDomain(true);
    			MyTitleAreaDialog newDomain = new MyTitleAreaDialog(asmInfo.getShell());
    			newDomain.create("Domain information", "Enter Domain name and description");
    			if (newDomain.open() == OKAY) {
    				asmInfo.setDomainName(newDomain.getDomainName());
    				asmInfo.setDescription(newDomain.getDescription());
    				if (newDomain.getDomainName() == "")
    				{
    					IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
    					MessageDialog.openInformation(window.getShell(),
        				"Information",
        				"Please, enter a domain name");
    				}
    				else 
    					asmUploadStatus = true;
    			}
    		}
    		else 
    		{
    			asmInfo.setNewDomain(false);
    			
    			try {
    				logger.info("Getting Domain List...");
					List<XmlDomain> domains = srv.listDomain(asmInfo.getCurrentProject());
					List<String> choices = new ArrayList<String>();
					List<String> domainIDList = new ArrayList<String>();
					
					for (XmlDomain xmlDomain : domains) {
			            choices.add(xmlDomain.getName());
			            domainIDList.add(xmlDomain.getId());
			        }
					
					String[] strOptions = choices.toArray(new String[0]);
					
					if (strOptions == null)
					{
						logger.info("There is no existing domain");
						IWorkbenchWindow window = HandlerUtil
								.getActiveWorkbenchWindowChecked(event);
            					MessageDialog.openInformation(window.getShell(),
		        				"Information",
		        				"There is no existing domain/n Please, use New Domain option");
					}
					else
					{
					
						
						ElementListSelectionDialog domainList =
							    new ElementListSelectionDialog(asmInfo.getShell(), new LabelProvider());
						domainList.setElements(strOptions);
						domainList.setTitle("Select a domain and click OK button");
						// user pressed cancel
						if (domainList.open() == OKAY) {
							Object[] result = domainList.getResult();
							if (result.length > 1)
							{
								IWorkbenchWindow window = HandlerUtil
										.getActiveWorkbenchWindowChecked(event);
	                					MessageDialog.openInformation(window.getShell(),
				        				"Information",
				        				"Please, select only one domain");
							} 
							else
							{
								String selectedDomain = result[0].toString();
								asmInfo.setDomainName(selectedDomain);
						    	asmInfo.setDomainID(domainIDList.get(findIndex(selectedDomain, choices)));
						    	asmUploadStatus = true;
							}
						}																								
					}
					
				} catch (AsmAccessDenied_Exception e) {
					logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
				} catch (AsmWebServiceFault_Exception e) {
					logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
				}
    		}			                		
		}
		else
		{
			asmUploadStatus = false; //cancel the workflow
		}
		
		return asmUploadStatus;
	}
	
	public static boolean getExistingASMDomain(String path, ProjectInfo asmInfo, ExecutionEvent event, AsmAdminService srv) throws ExecutionException
	{
		boolean asmUploadStatus = false;
    			
		try {
			logger.info("Getting Domain List...");
			List<XmlDomain> domains = srv.listDomain(asmInfo.getCurrentProject());
			List<String> choices = new ArrayList<String>();
			List<String> domainIDList = new ArrayList<String>();
			
			for (XmlDomain xmlDomain : domains) {
	            choices.add(xmlDomain.getName());
	            domainIDList.add(xmlDomain.getId());
	        }
			
			String[] strOptions = choices.toArray(new String[0]);
			
			if (strOptions == null)
			{
				logger.info("There is no existing domain");
				IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
    					MessageDialog.openInformation(window.getShell(),
        				"Information",
        				"There is no existing domain/n Please, use New Domain option");
			}
			else
			{
			
				
				ElementListSelectionDialog domainList =
					    new ElementListSelectionDialog(asmInfo.getShell(), new LabelProvider());
				domainList.setElements(strOptions);
				domainList.setTitle("Select a domain and click OK button");
				// user pressed cancel
				if (domainList.open() == OKAY) {
					Object[] result = domainList.getResult();
					if (result.length > 1)
					{
						IWorkbenchWindow window = HandlerUtil
								.getActiveWorkbenchWindowChecked(event);
            					MessageDialog.openInformation(window.getShell(),
		        				"Information",
		        				"Please, select only one domain");
					} 
					else
					{
						String selectedDomain = result[0].toString();
						asmInfo.setDomainName(selectedDomain);
				    	asmInfo.setDomainID(domainIDList.get(findIndex(selectedDomain, choices)));
				    	asmUploadStatus = true;
					}
				}																								
			}
			
		} catch (AsmAccessDenied_Exception e) {
			logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} catch (AsmWebServiceFault_Exception e) {
			logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		
		return asmUploadStatus;
	}
	
	public static boolean getASMProject(String path, ProjectInfo asmInfo, ExecutionEvent event, AsmAdminService srv) throws ExecutionException
	{
		// get Projects List
    	String selectedProject 		= null;
    	List<String> projects 		= null;
    	boolean asmUploadStatus		= false;
      
        try {
        	logger.info("Getting Projects List...");
        	Properties asmConnectionProps = new Properties();
            asmConnectionProps.load(new FileInputStream(path + ASM_PROPERTIES_FILE));
            String user = asmConnectionProps.getProperty("user");
            projects = srv.getProjectsByUserName(user);
			
			String[] strProjects = projects.toArray(new String[0]);
			
			ElementListSelectionDialog projectList =
				    new ElementListSelectionDialog(asmInfo.getShell(), new LabelProvider());
			projectList.setElements(strProjects);
			projectList.setTitle("Select a project and click OK button");
			// user pressed cancel
			if (projectList.open() == OKAY) {
				Object[] result = projectList.getResult();
				if (result.length > 1)
				{
					IWorkbenchWindow window = HandlerUtil
							.getActiveWorkbenchWindowChecked(event);
        					MessageDialog.openInformation(window.getShell(),
	        				"Information",
	        				"Please, select only one domain");
				} 
				else
				{
					selectedProject = result[0].toString();
					asmInfo.setCurrentProject(selectedProject);
					asmUploadStatus = true;
				}
			}
			else
			{
				asmUploadStatus = false; //cancel the workflow
			}
		} catch (AsmAccessDenied_Exception e) {
			logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
			window(event, "Warning", ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} catch (AsmWebServiceFault_Exception e) {
			logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
			window(event, "Warning", ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} catch (IOException e) {
			logger.debug("Warning" + ExceptionUtils.getStackTrace(e));
			window(event, "Warning", ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
        
        return asmUploadStatus;
	}
	
	public static void setupLogs(String projectFolder, ExecutionEvent event, Logger defaultLogger ) throws ExecutionException
	{
		 /*Setting logs 
         * by first reading log4j setting file 
         * or if no log4j file by setting logs by default   
         */    
		
        try {
        	// Reading log4j settings file
            Properties logConnectionProps = new Properties();
            logConnectionProps.load(new FileInputStream(projectFolder + LOG_PROPERTIES_FILE));
            PropertyConfigurator.configure(projectFolder + LOG_PROPERTIES_FILE);          
            logger.info("Initialized all properties from " + projectFolder + LOG_PROPERTIES_FILE);
        } catch (FileNotFoundException e) {
        	// Setting default logs => setting up a FileAppender dynamically...
    	    SimpleLayout layout = new SimpleLayout();    
    	    FileAppender appender = null;
    	    
    		try {
    			// Set all the log parameters
    			defaultLogger.setLevel((Level) Level.INFO);
              
    			//File appender
    			appender = new FileAppender(layout, projectFolder + "/ASM_Plugin_Log.log",true);
    			appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"));
    			defaultLogger.addAppender(appender);
    			
    			//Console appender
    			ConsoleAppender console = new ConsoleAppender();
    			console.setTarget("System.out");
    			String PATTERN = "%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n";
                console.setLayout(new PatternLayout(PATTERN));   
                console.activateOptions();
                defaultLogger.addAppender(console);
    			
    		} catch (IOException e1) {
    			window(event, "Warning", ExceptionUtils.getStackTrace(e1));
    			e1.printStackTrace();
    		}    	
        } catch (IOException e) {
        	logger.info("Log Properties file cannot be found");
        	window(event, "Warning", ExceptionUtils.getStackTrace(e));
        	e.printStackTrace();
        }
        
	}
	
	
	public static String getFilePath()
	{
		
		String projectPath 		= null;
		String filePath 	= null;
		
		projectPath = getProjectfolder();
	    
		IWorkbenchPage page 			= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection 			= page.getSelection();
		TreeSelection treeSelection 	= (TreeSelection) selection;
        TreePath[] treePaths 			= treeSelection.getPaths();
        int segmentCount 				= treePaths[0].getSegmentCount();
        filePath 					= treePaths[0].getSegment(segmentCount-1).toString();
        
        
        // Check if the correct folder has been selected by the user
        if (filePath.substring(0, 2).equalsIgnoreCase("F/") 
        		|| filePath.substring(0, 2).equalsIgnoreCase("L/"))
        {
	        int pos = filePath.indexOf("/");
	        if  ( pos != -1)
	        {
	        	filePath = filePath.substring(filePath.indexOf("/",2));
	        	filePath = projectPath + filePath;
	        }
        }
        else
        	filePath = null;
        
        return filePath;
	}
	
	
	public static String getProjectfolder()
	{
		String projectFolder = null;
		
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();

	    if(selection instanceof IStructuredSelection) {
	        Object element = ((IStructuredSelection)selection).getFirstElement();

	        if (element instanceof IResource) {
	        	projectFolder= ((IResource)element).getProject().getLocation().toString();
	        } else if (element instanceof IJavaElement) {
	            IJavaProject jProject= ((IJavaElement)element).getJavaProject();
	            projectFolder = jProject.getProject().getLocation().toString();
	        }
	    }
        
		return projectFolder;
	}
	
	private static int findIndex(String input, List<String> choices)
	{
		int index = 0;
    	for (int i=0; i< choices.size(); i++) {
            if (input.contentEquals(choices.get(i).toString()))
            {
            	index = i;
            	break;
            }
    	}
    	
    	return index;
	}
	
	private static IWorkbenchWindow window(ExecutionEvent event, String title, String message) throws ExecutionException
	{
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(),
				title,
				message); 
		
		
		return window;
	}
}
