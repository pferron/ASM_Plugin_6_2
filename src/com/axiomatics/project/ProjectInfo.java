package com.axiomatics.project;

import org.eclipse.swt.widgets.Shell;

public class ProjectInfo {
	
	private String 	domainName;
	private String 	description;
	private String 	policyPackageFile;
	private String 	projectFolder;
	private String  workspace;
	private Shell 	shell;
	private boolean bNewDomain;
	private String 	domainID;
	private String  currentProject;
	private String[] strProjects;
	
	
	public String[] getStrProjects()
	{
	    return this.strProjects;
	}
	public void setStrProjects(String[] strProjects)
	{
	     this.strProjects = strProjects;
	}
	
	public String getCurrentProject()
	{
	    return this.currentProject;
	}
	public void setCurrentProject(String currentProject)
	{
	     this.currentProject = currentProject;
	}
	
	public String getDomainID()
	{
	    return this.domainID;
	}
	public void setDomainID(String domainID)
	{
	     this.domainID = domainID;
	}
	
	public boolean isNewDomain()
	{
	    return this.bNewDomain;
	}
	public void setNewDomain(boolean bNewDomain)
	{
	     this.bNewDomain = bNewDomain;
	}
	
	public String getWorkSpace()
	{
	    return this.workspace;
	}
	public void setWorkSpace(String workspace)
	{
	     this.workspace = workspace;
	}
	
	public Shell getShell()
	{
	    return this.shell;
	}
	public void setShell(Shell shell)
	{
	     this.shell = shell;
	}
	
	public String getProjectFolder()
	{
	    return this.projectFolder;
	}
	public void setprojectFolder(String projectFolder)
	{
	     this.projectFolder = projectFolder;
	}

	public String getPolicyPackageFile()
	{
	    return this.policyPackageFile;
	}
	public void setPolicyPackageFile(String policyPackageFile)
	{
	     this.policyPackageFile = policyPackageFile;
	}
	
	public String getDomainName()
	{
	    return this.domainName;
	}
	public void setDomainName(String domainName)
	{
	     this.domainName = domainName;
	}
	
	public String getDescription()
	{
	    return this.description;
	}
	public void setDescription(String description)
	{
	     this.description = description;
	}

}
