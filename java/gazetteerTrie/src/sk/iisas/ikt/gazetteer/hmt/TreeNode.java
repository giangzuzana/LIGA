package sk.iisas.ikt.gazetteer.hmt;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Node in trie of characters (HMT)
 * Author: Giang Nguyen (2013, multiway tree using hash map)
 */

public class TreeNode {
	
	private Character character;
	private HashMap<Character, TreeNode> childNodes;
	private String entityName = "";		// pokial je to list, bude vyplnene, inak bude ""
	private String entityURI  = "";		// entityURI=(URI1 medzera URI2 ...),  
	
	public TreeNode(Character character) {		
		this.character	= character;
		this.childNodes = new HashMap<Character, TreeNode>();	
	}

	public Character getCharacter() {	return character;	    }
	public String	 getEntityURI() {	return entityURI;		}
	public String	 getEntityName() {	return entityName;		}

	public HashMap<Character, TreeNode> getChildNodes() 					{		return childNodes;	}
	public void setChildNodes( HashMap<Character, TreeNode> childNodes)		{		this.childNodes = childNodes;	}
	
	public void setCharacter(Character character) {		this.character	= character;		}
	public void setEntityName(String entityName) {		this.entityName	= entityName;		}

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
		if (this.entityName.length() > 0)
			return true;
		return false;
	}
}
