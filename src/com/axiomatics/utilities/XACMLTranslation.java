package com.axiomatics.utilities;

public class XACMLTranslation {
	
	public static String XacmlEquivalentType(String type)
    {
    	switch(type)
    	{
    	   case "string" :
    		  type = "http://www.w3.org/2001/XMLSchema#string";
    	      break; 	    	   
    	   case "boolean" :
    		  type = "http://www.w3.org/2001/XMLSchema#boolean";
    	      break; 
    	   case "anyURI" :
     		  type = "http://www.w3.org/2001/XMLSchema#anyURI";
     	      break; 
    	   case "date" :
      		  type = "http://www.w3.org/2001/XMLSchema#date";
      	      break; 
    	   case "dateTime" :
       		  type = "http://www.w3.org/2001/XMLSchema#dateTime";
       	      break; 
    	   case "dnsName" :
    		  type = "urn:oasis:names:tc:xacml:2.0:data-type:dnsName";
        	  break; 
    	   case "double" :
     		  type = "http://www.w3.org/2001/XMLSchema#double";
         	  break; 
    	   case "integer" :
     		  type = "http://www.w3.org/2001/XMLSchema#integer";
         	  break; 
    	   case "ipAddress" :
     		  type = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";
         	  break; 
    	   case "time" :
     		  type = "http://www.w3.org/2001/XMLSchema#time";
         	  break; 
    	   case "dayTimeDuration" :
     		  type = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";
         	  break; 
    	   case "yearMonthDuration" :
      		  type = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";
          	  break; 
    	   case "x500Name" :
      		  type = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";
          	  break; 
    	   case "rfc822Name" :
      		  type = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";
          	  break; 
    	   case "hexBinary" :
      		  type = "http://www.w3.org/2001/XMLSchema#hexBinary";
          	  break; 
    	   case "base64Binary" :
      		  type = "http://www.w3.org/2001/XMLSchema#base64Binary";
          	  break; 
    	   case "xpath" :
      		  type = "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression";
          	  break;           	 
    	}
    	
    	return type;
    }
   

   public static String XacmlEquivalentCategory(String category)
   {
	   	switch(category)
	   	{
	   	   case "subjectCat" :
	   		   category = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	   	      break; 	    	   
	   	   case "resourceCat" :
	   		   category = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	   	      break; 
	   	   case "actionCat" :
	   		   category = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	    	      break; 
	   	   case "environmentCat" :
	   		   category = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
	   	}
	   	
	   	return category;
   }
	
	public static String reverseXacmlEquivalentType(String type)
    {
    	switch(type)
    	{
    	   case "http://www.w3.org/2001/XMLSchema#string" :
    		  type = "string";
    	      break; 	    	   
    	   case "http://www.w3.org/2001/XMLSchema#boolean" :
    		  type = "boolean";
    	      break; 
    	   case "http://www.w3.org/2001/XMLSchema#anyURI" :
     		  type = "anyURI";
     	      break; 
    	   case "http://www.w3.org/2001/XMLSchema#date" :
      		  type = "date";
      	      break; 
    	   case "http://www.w3.org/2001/XMLSchema#dateTime" :
       		  type = "dateTime";
       	      break; 
    	   case "urn:oasis:names:tc:xacml:2.0:data-type:dnsName" :
    		  type = "dnsName";
        	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#double" :
     		  type = "double";
         	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#integer" :
     		  type = "integer";
         	  break; 
    	   case "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress" :
     		  type = "ipAddress";
         	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#time" :
     		  type = "time";
         	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#dayTimeDuration" :
     		  type = "dayTimeDuration";
         	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#yearMonthDuration" :
      		  type = "yearMonthDuration";
          	  break; 
    	   case "urn:oasis:names:tc:xacml:1.0:data-type:x500Name" :
      		  type = "x500Name";
          	  break; 
    	   case "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name" :
      		  type = "rfc822Name";
          	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#hexBinary" :
      		  type = "hexBinary";
          	  break; 
    	   case "http://www.w3.org/2001/XMLSchema#base64Binary" :
      		  type = "base64Binary";
          	  break; 
    	   case "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression" :
      		  type = "xpath";
          	  break;           	 
    	}
    	
    	return type;
    }
   

   public static String reverseXacmlEquivalentCategory(String category)
   {
	   	switch(category)
	   	{
	   	   case "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject" :
	   		   category = "subjectCat";
	   	      break; 	    	   
	   	   case "urn:oasis:names:tc:xacml:3.0:attribute-category:resource" :
	   		   category = "resourceCat";
	   	      break; 
	   	   case "urn:oasis:names:tc:xacml:3.0:attribute-category:action" :
	   		   category = "actionCat";
	    	      break; 
	   	   case "urn:oasis:names:tc:xacml:3.0:attribute-category:environment" :
	   		   category = "environmentCat";
	   	}
	   	
	   	return category;
   }

}
