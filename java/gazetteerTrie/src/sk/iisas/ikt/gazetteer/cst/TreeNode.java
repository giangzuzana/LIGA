package sk.iisas.ikt.gazetteer.cst;
import java.util.Arrays;

/**
 * Node in trie of characters (CST)
 * Author: Giang Nguyen (2013)
 * Author: Stefan Dlugolinsky (2013, first-child next-sibling binary tree for trie compact)
 */

public class TreeNode {
	
	private Character	character;
	private String		entityURI	= "";		// entityURI=(URI1 medzera URI2 ...) 
	private String		entityName	= "";		// pokial je to list, bude vyplnene, inak bude ""
	private TreeNode	siblingNode = null;		// first sibling node
	private TreeNode	childNode	= null;		// first child node
	
	public Character getCharacter()		{	return character;	}
	public String	getEntityURI()		{	return entityURI;	}
	public String	getEntityName()		{	return entityName;	}
	public TreeNode	getSiblingNode()	{	return siblingNode;	}
	public TreeNode	getChildNode()		{	return childNode;	}
	
	public void setCharacter(Character character) {		this.character	= character;	}
	
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
	public void setEntityName(	String	 entityName)	{	this.entityName  = entityName;	}
	public void setSiblingNode( TreeNode siblingNode)	{	this.siblingNode = siblingNode;	}
	public void setChildNode( 	TreeNode childNode)		{	this.childNode   = childNode;	}
	
	public boolean isListNode() {
		if (this.entityName.length() > 0)
			return true;
		return false;
	}
}
