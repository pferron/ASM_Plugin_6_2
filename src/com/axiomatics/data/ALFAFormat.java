package com.axiomatics.data;

import java.util.List;

public class ALFAFormat {
	
	private String nameSpace;
	private List<ALFAAttribute> attribute;
	
	public String getNamespace()
	{
	    return this.nameSpace;
	}
	public void setNamespace(String nameSpace)
	{
	     this.nameSpace = nameSpace;
	}
	
	public void setAttribute(List<ALFAAttribute> attribute) {
		this.attribute = attribute;
	}
    
    public List<ALFAAttribute> getAttribute() {
		return this.attribute;
	}

}
