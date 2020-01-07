package com.axiomatics.data;

import java.util.List;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "AttributeDictionary")
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeDictionary {

    @XmlTransient
 	private String xmlns;
    private List<Attribute> Attribute;
    
    @XmlAttribute
	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}
    public String getXmlns() {
		return this.xmlns;
	}
    
//    @XmlElement
    public void setAttribute(List<Attribute> Attribute) {
		this.Attribute = Attribute;
	}
    
    public List<Attribute> getAttribute() {
		return this.Attribute;
	}
}
