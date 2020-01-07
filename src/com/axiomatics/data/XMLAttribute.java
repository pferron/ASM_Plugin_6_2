package com.axiomatics.data;


public class XMLAttribute{
	
	private String nameSpace					= null;
	private String id							= null;
	private String attributeName				= null;	
	private String datatype						= null;
	private String category		 				= null;
	private String description					= null;


	public String getDescription()
	{
	    return this.description;
	}
	public void setDescription(String description)
	{
	     this.description = description;
	}
	
	public String getId()
	{
	    return this.id;
	}
	public void setId(String id)
	{
	     this.id = id;
	}
	
	public String getName()
	{
	    return this.attributeName;
	}
	public void setName(String attributeName)
	{
	     this.attributeName = attributeName;
	}
	
	public String getNamespace()
	{
	    return this.nameSpace;
	}
	public void setNamespace(String nameSpace)
	{
	     this.nameSpace = nameSpace;
	}
	
	public String getDatatype()
	{
	    return this.datatype;
	}
	public void setDatatype(String datatype)
	{
	     this.datatype = datatype;
	}
	
	public String getCategory()
	{
	    return this.category;
	}
	public void setCategory(String category)
	{
	     this.category = category;
	}

}
