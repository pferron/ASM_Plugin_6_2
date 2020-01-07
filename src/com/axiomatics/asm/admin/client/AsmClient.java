package com.axiomatics.asm.admin.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import javax.xml.ws.WebServiceException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.axiomatics.asm.client.AsmAdminClient;
import com.axiomatics.asm.client.ClientInfo;
import com.axiomatics.data.ALFAAttribute;
import com.axiomatics.data.XMLAttribute;
import com.axiomatics.project.ProjectInfo;

public class AsmClient {
	
	private static final String PROPERTIES_FILE = "/asm.properties";
	
	public static ClientInfo clientInfo;	
	public static AsmAdminService service = null;    	
	static Logger logger = Logger.getLogger(AsmClient.class.getName());
	
	public static void createXMLAttributeInDictionary(List<XMLAttribute> attributeList,
			String currentProject, 
			AsmAdminService srv) throws AsmAccessDenied_Exception, AsmWebServiceFault_Exception, IOException {

		for(int i=0;i<attributeList.size();i++)
		{
		TxAttribute createAttribute = srv.createAttribute(
								attributeList.get(i).getId(), 
								attributeList.get(i).getName(),
								attributeList.get(i).getNamespace(),
								attributeList.get(i).getDatatype(),
								attributeList.get(i).getCategory(),
								currentProject);
		
		createAttribute.description = attributeList.get(i).getDescription();
		srv.updateAttribute(createAttribute, currentProject);
		
		System.out.println("ALFAAttribute Created successfully");
		System.out.println("ALFAAttribute id: " + createAttribute.getXacmlId());
		System.out.println("ALFAAttribute name: " + createAttribute.getName());
		System.out.println("ALFAAttribute namespace: " + createAttribute.getNamespace());
		System.out.println("ALFAAttribute category: " + createAttribute.getCategory());
		System.out.println("ALFAAttribute data type: " + createAttribute.getXacmlDataType());
		System.out.println("ALFAAttribute description: " + createAttribute.getDescription());
		}
	}
	
	
	public static void createALFAAttributeInDictionary(List<ALFAAttribute> attributeList,
														String currentProject, 
														AsmAdminService srv) throws AsmAccessDenied_Exception, AsmWebServiceFault_Exception, IOException {

		for(int i=0;i<attributeList.size();i++)
   		{
				TxAttribute createAttribute = srv.createAttribute(
																	attributeList.get(i).getXacmlId(), 
																	attributeList.get(i).getXacmlAttributeName(),
																	attributeList.get(i).getXacmlNameSpace(),
																	attributeList.get(i).getXacmlDataType(),
																	attributeList.get(i).getXacmlCategory(),
																	currentProject);
				
				createAttribute.description = attributeList.get(i).getXacmlDescription();
				srv.updateAttribute(createAttribute, currentProject);
				
				System.out.println("ALFAAttribute Created successfully");
				System.out.println("ALFAAttribute id: " + createAttribute.getXacmlId());
				System.out.println("ALFAAttribute name: " + createAttribute.getName());
				System.out.println("ALFAAttribute namespace: " + createAttribute.getNamespace());
				System.out.println("ALFAAttribute category: " + createAttribute.getCategory());
				System.out.println("ALFAAttribute data type: " + createAttribute.getXacmlDataType());
				System.out.println("ALFAAttribute data type: " + createAttribute.getDescription());
		}
	}
    
    
    public static AsmAdminService getService(String projectPath, ProjectInfo asmInfo, ExecutionEvent event) throws ExecutionException {

    	if (clientInfo == null)
  		   initProperties(projectPath);
  	   
  	   	logger.info("====== Getting web service client ======");
  	   
        // Get the JAX-WS connection to ASM. The following is the actual entrypoint to the client
        // library provided.
        AsmAdminClient asmAdminClient = new AsmAdminClient(clientInfo);
        try {
        	service = asmAdminClient.getConnection();
        
        } catch (WebServiceException e) {
			
			// Message : ASM is expected to run.
    		logger.info("Unable to connect to the ASM Web Service - Please, verify credentials in the ASM properties file in your ALFA project"); 
			IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
    		MessageDialog.openInformation(window.getShell(),
    				"Information",
    				"Unable to connect to the ASM Web Service - Please, verify credentials in the ASM properties file in your ALFA project"); 
    		
		} catch (ConnectException e) {
			
            // Message : ASM is expected to run.
			logger.info("Unable to connect to ASM - Please, make sure ASM is running"); 
			IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
    		MessageDialog.openInformation(window.getShell(),
    				"Information",
    				"Unable to connect to ASM - Please, make sure ASM is running"); 

		} catch (UnknownHostException e) {
			
			// Message : ASM is expected to run.
			logger.info("Unable to connect to the ASM Web Service - Please, verify ASM URL in the ASM properties file in your ALFA project"); 
			IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
    		MessageDialog.openInformation(window.getShell(),
    				"Information",
    				"Unable to connect to the ASM Web Service - Please, verify ASM URL in the ASM properties file in your ALFA project"); 
		
		} catch (FileNotFoundException e) {
			
			// Message : ASM is expected to run.
			logger.info("Unable to find keystore - Please, verify keystore path in the ASM properties file in your ALFA project");
			IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
    		MessageDialog.openInformation(window.getShell(),
    				"Information",
    				"Unable to find keystore - Please, verify keystore path in the ASM properties file in your ALFA project"); 
    		
		} catch (Exception e) {
			logger.debug("Information" + ExceptionUtils.getStackTrace(e));
			IWorkbenchWindow window = HandlerUtil
					.getActiveWorkbenchWindowChecked(event);
					MessageDialog.openInformation(window.getShell(),
    				"Information",
    				ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
        
        return service;
    }
    
    
	public static void sendPolicyPackage(ProjectInfo asmInfo, ExecutionEvent event) throws Exception {
    	
    	XmlDomain newDomain = null;
    	String currentProject = null;
    	
    	if (service == null)
    		service = AsmClient.getService(asmInfo.getWorkSpace() + asmInfo.getProjectFolder(), asmInfo, event);
    	
        try {

        	logger.info("====== Testing Connection ======");
            String testConnection = service.testConnection();
            logger.info("--- TEST CONNECTION ---" + testConnection);

            logger.info("====== Getting Selected Project ======");
            currentProject = asmInfo.getCurrentProject();
            logger.info("Selected Project: " + currentProject);
            
            boolean bNewDomain = asmInfo.isNewDomain();      
            
            if ( bNewDomain == true)
            {
            	logger.info("====== Creating and Updating domain ======");
	            
	            newDomain = service.createDomain(currentProject);
	            logger.info("New domain created ");
	            logger.info("Domain name: " + newDomain.getName());

	            // Update the newly created Domain
	            newDomain.setName("New Domain");
	            newDomain.setDescription("This domain is created by the admin client SOAP API for testing purpose");
	            service.updateDomainConfiguration(newDomain, currentProject);		            
	            asmInfo.setDomainID(newDomain.getId());
		        
		        
		        // Update the newly created Domain
		        logger.info("------------ UPDATE Domain INFORMATION-------------");
		        newDomain.setName(asmInfo.getDomainName());
		        newDomain.setDescription(asmInfo.getDescription());
		        service.updateDomainConfiguration(newDomain, asmInfo.getCurrentProject());
		        logger.info("Domain name: " + newDomain.getName());
		        logger.info("Domain description: " + newDomain.getDescription());
            }

            logger.info("====== Uploading policy to domain ======");
            File policyZip = new File(asmInfo.getPolicyPackageFile());
            byte[] policyStr = new byte[(int) policyZip.length()];
            InputStream is = new FileInputStream(policyZip);
            int numByte = is.read(policyStr);
            is.close();
            TransferablePolicyPackage ppkg = new TransferablePolicyPackage();
            ppkg.setPolicyBytes(policyStr);
            logger.debug("Number of byte read: " + numByte);
            service.assignPolicyToDomainViaPackage(ppkg, asmInfo.getDomainID(), currentProject);
            logger.info("Upload successful");

            logger.info("====== Listing all domains ======");
            List<XmlDomain> domains = service.listDomain(currentProject);
            for (XmlDomain xmlDomain : domains) {
            	logger.info("Domain: " + xmlDomain.getName());
            }

        } catch (Exception e) {
            // This is only a test program using main, so just logging the stacktrace to the console
            e.printStackTrace();
            System.exit(-1);
        }
    }
	
    
    private static void initProperties(String projectPath) {
    	
        try {
            Properties asmConnectionProps = new Properties();
            asmConnectionProps.load(new FileInputStream(projectPath + PROPERTIES_FILE));

            // Set all the ASM runtime parameters
            clientInfo = new ClientInfo(asmConnectionProps.getProperty("asmUrl"));
            clientInfo.setUser(asmConnectionProps.getProperty("user"));
    		clientInfo.setWsdlUrl(asmConnectionProps.getProperty("wsdlUrl"));
    		clientInfo.setPassword(asmConnectionProps.getProperty("password"));
    		if (asmConnectionProps.getProperty("trustStore")!=null){
    		    clientInfo.setTrustStoreType(asmConnectionProps.getProperty("trustStoreType"));
    		    clientInfo.setTrustStore(asmConnectionProps.getProperty("trustStore"));
    		    clientInfo.setTrustStorePassword(asmConnectionProps.getProperty("trustStorePassword"));
    		}
            logger.info("Initialized all properties from " + PROPERTIES_FILE);
        } catch (FileNotFoundException e) {
        	logger.info("Properties file cannot be found");
            printUsage();
            System.exit(-1);
        } catch (IOException e) {
            printUsage();
            System.exit(-1);
        }
    }
    
    private static void printUsage() {
    	logger.info("Usage: asm_connection.properties should be provided in classpath (Or)"
                + AsmClient.class.getSimpleName()
                + " <asmUrl> <user> <password> <trustStoreType> <trustStore> <trustStorePassword> <wsldUrl> <attributeDictionnaryFilename> <projectName>");

    }

}
