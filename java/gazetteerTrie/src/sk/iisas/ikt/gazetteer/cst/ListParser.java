package sk.iisas.ikt.gazetteer.cst;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 * Parser list of entities from input file to gazetteer's trie structure (CST)
 * Author: Giang Nguyen (2013)
 * Author: Stefan Dlugolinsky (2013, first-child next-sibling binary tree for trie compact)
 */

public class ListParser {
	//private static final Logger logger = Logger.getLogger("sk.iisas.ikt.tokenizer.impl.ListParser");

	public TreeNode parseListToTree(String filePath) {
		TreeNode rootNode = new TreeNode();

		try {
			FileInputStream fstream = new FileInputStream(filePath);			
			Reader 			reader	= new InputStreamReader(fstream, "UTF-8");	
			BufferedReader	br		= new BufferedReader(reader);				// buffer for efficiency
			String strLine;
			int lineNumber = 0;
			int charNumber = 0;
			int nodeNumber = 0;
			
			while ((strLine = br.readLine()) != null) {							// Read file line by line

				TreeNode currentNode = rootNode;
				String name="";
				String uri="";
				lineNumber++;
				if (lineNumber % 100000 == 0)	System.out.println(lineNumber + "\t" + charNumber + "\t" + nodeNumber);
				
				// FreeBase input files (from Selo)
				String[] tokens = strLine.split("\t");		// riadok=(URI \t type \t name)	
				uri  = tokens[0];							// URI
				name = tokens[2];							// name entity
				
				/* // IAB Magnetic input
				if (strLine.charAt(0) == '\t') {
					String[] tokens = strLine.split("\t");		// riadok=(\t name \t weight)
					name = tokens[1];							// name				
					uri  = tokens[2];							// URI	
					// System.out.println("name=" + name + "\t" + "uri=" + uri);
				} else continue;
				*/
				
				name = name.trim().replaceAll("\\s+", " ");			// normalize white characters
				if (name.length() <= 3) 		continue;			// strom vytvorime len z mien dlhsich ako 3 znaky
				if (containTurkishChars(name))	continue;			// ani Turkish chars :D

				for (int index = 0; index < name.length(); index++) {			
					Character charAtIndex = Character.toLowerCase(name.charAt(index));
					charNumber++;
					
					TreeNode nextNode = findChildWithChar(currentNode, charAtIndex);		
					if (nextNode != null ){							// ked node ma take child, tak sa posunume dole
						currentNode = nextNode;
					} else {										// ak nema takeho child, tak pridame		
						nodeNumber++;
						TreeNode newNode = new TreeNode();
						newNode.setCharacter(charAtIndex);
						addNewChildNode(currentNode, newNode);
						currentNode = newNode;
					}
				}
				// sme na konci riadku, pridame name a URI entity
				currentNode.addEntityURI(uri);						
				currentNode.setEntityName(name);											
			}
			System.out.println("Number of lines= " + lineNumber + "\tNumber of chars= " + charNumber + "\tNumber of nodes= " + nodeNumber);
			br.close();				
			return rootNode;

		} catch (Exception e) {
            //logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public TreeNode findChildWithChar(TreeNode currentNode, Character readingChar) {
		TreeNode childNode = currentNode.getChildNode();
		if (childNode != null) {
			if (childNode.getCharacter() == readingChar) {
				return childNode;
			} else {
				while (childNode.getSiblingNode() != null) {
					childNode = childNode.getSiblingNode();
					if (childNode.getCharacter() == readingChar) 
						return childNode;
				}
			}
		} 
		return null;
    }
	
	public void addNewChildNode(TreeNode currentNode, TreeNode newNode) {
		TreeNode childNode = currentNode.getChildNode();
		if (childNode == null) {
			currentNode.setChildNode(newNode);
		} else {
			while (childNode.getSiblingNode() != null) {
				childNode = childNode.getSiblingNode();		
			}
			childNode.setSiblingNode(newNode);
		}
	}
	
	private boolean containTurkishChars(String str) {
		return str.matches(".*[ıİşŞğĞüÜöÖçÇ].*");
	}
	
	public static void main(String[] args) {
		ListParser lp = new ListParser();
		lp.parseListToTree("data/crosby-gazetteer.txt");
	}
}
