package sk.iisas.ikt.gazetteer.pht;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Node in trie of characters (PHT Patricia Hashmap Tree)
 * Author: Giang Nguyen (2013)
 */

public class TreeNode {
	
	private String charList ="";	
	private HashMap<Character, TreeNode> childNodes;
	private String entityURI  = "";		// entityURI=(URI1 medzera URI2 ...) 
	private String entityName = "";		// pokial je to list, bude vyplnene, inak bude ""
	
	public TreeNode() 					{	this.childNodes = new HashMap<Character, TreeNode>();	}
	public TreeNode(String charList)	{	
		this.charList   = charList;
		this.childNodes = new HashMap<Character, TreeNode>();	
	}

	public String getCharList()		{	return charList;	}
	public String getEntityURI()	{	return entityURI;	}
	public String getEntityName()	{	return entityName;	}

	public HashMap<Character, TreeNode> getChildNodes() 				{	return childNodes;				}
	public void setChildNodes( HashMap<Character, TreeNode> childNodes)	{	this.childNodes = childNodes;	}
	
	public void setCharList(String charList)		{	this.charList  = charList;		}
	public void addChar(Character ch)		{	this.charList += ch;		}
	
	public void setEntityName(String entityName)	{	this.entityName	= entityName;	}
	public void setEntityURI(String entityURI) 		{	this.entityURI	= entityURI;	}
	public void addEntityURI(String entityURI) {
		if (this.entityURI.length() > 0) {
			String[] token = this.entityURI.split("\\s+");		
			
			if (!Arrays.asList(token).contains(entityURI)) 
				this.entityURI = this.entityURI + " " + entityURI;
		} else {
			this.entityURI = entityURI;
		}
		// System.out.println(this + " name=" + this.getObjectName() + " URI=" + this.getObjectURI());
	}
	
	public boolean isListNode() {
		if (this.getEntityName().length() > 0)
			 return true;
		else return false;
	}
	
}
