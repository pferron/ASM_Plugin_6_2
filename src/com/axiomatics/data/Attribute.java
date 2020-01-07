package com.axiomatics.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Attribute")
@XmlType(propOrder={"name", "namespace" , "id", "description", "category", "datatype" })
public class Attribute {
	
	private String name;
	private String nameSpace;
	private String id;
	private String description;
	private String category;
	private String dataType;
	
	@XmlElement(name = "Description")
	public String getDescription()
	{
	    return this.description;
	}
	public void setDescription(String description)
	{
	     this.description = description;
	}
	
	@XmlElement(name = "Id")
	public String getId()
	{
	    return this.id;
	}
	public void setId(String id)
	{
	     this.id = id;
	}
	
	@XmlElement(name = "Name")
	public String getName()
	{
	    return this.name;
	}
	public void setName(String name)
	{
	     this.name = name;
	}
	
	@XmlElement(name = "Namespace")
	public String getNamespace()
	{
	    return this.nameSpace;
	}
	public void setNamespace(String nameSpace)
	{
	     this.nameSpace = nameSpace;
	}
	
	@XmlElement(name = "Datatype")
	public String getDatatype()
	{
	    return this.dataType;
	}
	public void setDatatype(String dataType)
	{
	     this.dataType = dataType;
	}
	
	@XmlElement(name = "Category")
	public String getCategory()
	{
	    return this.category;
	}
	public void setCategory(String category)
	{
	     this.category = category;
	}

	

}
