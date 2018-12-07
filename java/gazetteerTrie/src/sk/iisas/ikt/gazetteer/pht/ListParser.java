package sk.iisas.ikt.gazetteer.pht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser list of entities from input file to gazetteer's trie structure (PHT Patricia Hashmap Tree)
 * Author: Giang Nguyen (2013)
 */

public class ListParser {

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
			int errNumber  = 0;
			//BufferedWriter writer = new BufferedWriter(new FileWriter("data/daniel-gaz.txt"));

			while ((strLine = br.readLine()) != null) {							// Read file line by line
	
				lineNumber++;
				TreeNode currentNode = rootNode;
				String name="";
				String uri="";	
						
				// FreeBase input files (from Selo)
				String[] tokens = strLine.split("\t");			// riadok=(URI \t type \t name)	
				uri  = tokens[0];								// URI
				name = tokens[2];								// name entity
				
				/* // IAB Magnetic input
				if (strLine.charAt(0) == '\t') {
					String[] tokens = strLine.split("\t");		// riadok=(\t name \t weight)
					name = tokens[1];							// name				
					uri  = tokens[2];							// URI	
					// System.out.println("name=" + name + "\t" + "uri=" + uri);
				} else continue;
				*/
				
				//if (name.toLowerCase().startsWith("slav"))	writer.write(strLine + "\n");
				
				if (lineNumber % 100000 == 0)	System.out.println(lineNumber + "\t" + charNumber + "\t" + nodeNumber);
				
				name = name.trim().replaceAll("\\s+", " ");		// normalize white characters
				if (name.length() <= 3) 		continue;		// strom vytvorime len z entit dlhsich ako 3 znaky
				if (containTurkishChars(name))	continue;		// ani Turkish chars :D
				
				Character charAtIndex=null;
				String readOut = "";							// precitana cast entity
				String reading = "";
				
				for (int index = 0; index < name.length(); index++) {			
					charNumber++;
					charAtIndex = Character.toLowerCase(name.charAt(index));
					reading	    = setReading(name, index, readOut);
					String charList	= currentNode.getCharList();
					
					//if (currentNode.equals(rootNode))	System.out.print("\n");
					//System.out.println("CHAR=" + charAtIndex + "\tREADING=" + reading +  "\tCHARLIST=" + charList);							
					
					if (reading.endsWith(charList)){						// reading.len >= charList.len
						if (( currentNode.getChildNodes().size() == 0) && (!currentNode.isListNode()) && (!currentNode.equals(rootNode)) ){
							currentNode.addChar(charAtIndex);										// pridame char do zoznamov
						} else {
							readOut += currentNode.getCharList();					// zapisem si "cestu" cez uzly skor ako prejdem na child node
							reading = setReading(name, index, readOut);				
							
							if (currentNode.getChildNodes().containsKey(charAtIndex) ){				// ked node ma take child 
								currentNode = currentNode.getChildNodes().get(charAtIndex);					// tak sa posunme dole
							} else {																// ak nema takeho child 
								nodeNumber++;
								TreeNode newNode = new TreeNode( Character.toString(charAtIndex) );			// tak ho pridame 
								currentNode.getChildNodes().put(charAtIndex, newNode);						// a posunme k novemu nodu			
								currentNode = newNode;
							}
						} 
					} else { 												// kde sa rozchadza znak, tam delim na na uzly (remain a new)													
						if ( !charList.startsWith(reading+charAtIndex) && charList.startsWith(reading) ) {	// reading.len < charList.len						
						
							nodeNumber++;							
							TreeNode newNodeForRemain = new TreeNode();				// new node for remain part
							setTreeNodes(currentNode, newNodeForRemain, reading);							

							readOut += currentNode.getCharList();					// zapisem si "cestu" cez uzly skor ako prejdem na new char node
							reading = setReading(name, index, readOut);

							nodeNumber++;											// new node for new char
							TreeNode newNode = new TreeNode( Character.toString(charAtIndex) );
							currentNode.getChildNodes().put(charAtIndex, newNode);					
							currentNode = newNode;
						} else {
							//System.out.println("??? CHAR=" + charAtIndex + "\tREADING=" + reading +  "\tCHARLIST=" + charList);
						}
					}
				} 		
				// na konci riadku, ak citana entita je kratsia ako existujuca, tak treba sa rozdelit uprostred
				String readingStr = reading + charAtIndex;
				if (currentNode.getCharList().startsWith(readingStr) && (currentNode.getCharList().length() > readingStr.length()) ){
					nodeNumber++;							
					TreeNode newNodeForRemain = new TreeNode();
					setTreeNodes(currentNode, newNodeForRemain, readingStr);							
				}
				currentNode.addEntityURI(uri);	

				if ( currentNode.getEntityName().equals("") ) {
					currentNode.setEntityName(name); 

				} else if (! currentNode.getEntityName().toLowerCase().equals(name.toLowerCase()) ) {
					// check ERROR - Turkish chars problem HERE
					errNumber++;
					System.out.println("\tERROR: name=" + name + "\tnode=" + currentNode.getCharList() + "-->" + currentNode.getEntityName() );
				} 
			}
			//writer.close();
			//System.out.println("\n TREE IS:");
			//printTree(rootNode, 0);
			
			System.out.println("\n PARSING DONE: ERR=" + errNumber + "\tLines=" + lineNumber + "\tChars=" + charNumber + "\tNodes=" + nodeNumber);
			br.close();				
			return rootNode;

		} catch (Exception e) {
            //logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private void setTreeNodes(TreeNode currentNode, TreeNode newNodeForRemain, String reading) {
		
		if (currentNode.getCharList().length() > reading.length()) {
			String remainPart = currentNode.getCharList().substring(reading.length());
		
			newNodeForRemain.setCharList(remainPart);								// remaining part to new node
			newNodeForRemain.setEntityName(currentNode.getEntityName());			// move NAME from currentNode to new node
			newNodeForRemain.setEntityURI(currentNode.getEntityURI());				// move URI  from currentNode to new node
			
			HashMap<Character, TreeNode> tmp = newNodeForRemain.getChildNodes();	// move childNode list from currentNode to new node
			newNodeForRemain.setChildNodes(currentNode.getChildNodes());
			currentNode.setChildNodes(tmp);
			
			currentNode.getChildNodes().put(remainPart.charAt(0), newNodeForRemain);
			currentNode.setCharList(reading) ;
			currentNode.setEntityName("");
			currentNode.setEntityURI("");
			
			//System.out.println("\t\tCURRENT NODE=" 	+ currentNode.getCharList() 	+ "-->" + currentNode.getEntityName()); 
			//System.out.println("\t\tADD REMAIN NODE=" + newNodeForRemain.getCharList()+ "-->" + newNodeForRemain.getEntityName());
		} else {
			//System.out.println("\t\t\t ???");
		}
	}
	
	private String setReading(String nameEntity, int index, String readOut) {
		return nameEntity.toLowerCase().substring(0, index).substring(readOut.length());
	}
	
	private boolean containTurkishChars(String str) {
		return str.matches(".*[ıİşŞğĞüÜöÖçÇ].*");
	}
	
	public void printTree(TreeNode node, int level){		
		
		level++;
	    for (int i=0; i < level; i++)	System.out.print("\t");
	    
        if (node.isListNode()) {
        	String charList = node.getCharList();
        	String name = node.getEntityName();
	    	System.out.print(node.getCharList() + "-->" + node.getEntityName());
	    	
	    	if ( !name.toLowerCase().endsWith(charList.toLowerCase()) )
	    		 System.out.println("\tWHAT HERE");
	    	else System.out.println("");
	    		    	
        } else {
        	System.out.println(node.getCharList() + "-->" + "(F)");
        }
 
//		if (node.getEntityName().toLowerCase().equals("slav"))
//        	System.out.println(node.getCharList() + "-->" + node.getEntityName());
		
	    for (Map.Entry<Character, TreeNode> entry : node.getChildNodes().entrySet())  
	    	 printTree((TreeNode) entry.getValue(), level);
	}
	
	public static void main(String[] args) {
		ListParser lp = new ListParser();
		lp.parseListToTree("data/persons_tripples.txt");
		//lp.parseListToTree("data/crosby-gaz.txt");
		//lp.parseListToTree("data/turkish-gaz.txt");
		//lp.parseListToTree("data/slav-gaz.txt");
	}
}
