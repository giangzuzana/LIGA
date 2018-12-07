package sk.iisas.ikt.gazetteer.hmt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Parser list of entities from input file to gazetteer's trie structure (HMT)
 * Author: Giang Nguyen (2013)
 */

public class ListParser {

	public TreeNode parseListToTree(String filePath) {
		TreeNode rootNode = new TreeNode(null);

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
				
				name = name.trim().replaceAll("\\s+", " ");		// normalize white characters
				if (name.length() <= 3) continue;				// strom vytvorime len z entit dlhsich ako 3 znaky
				if (containTurkishChars(name))	continue;		// ani Turkish chars :D

				for (int index = 0; index < name.length(); index++) {			
					Character charAtIndex = Character.toLowerCase(name.charAt(index));
					charNumber++;
													
					if (currentNode.getChildNodes().containsKey(charAtIndex)){		// ked node ma take child, tak sa posunume dole v strome
						currentNode = currentNode.getChildNodes().get(charAtIndex);
					} else {														// este take child/char nema, vlozime new child						
						nodeNumber++;
						TreeNode newNode = new TreeNode(charAtIndex);	
						currentNode.getChildNodes().put(charAtIndex, newNode);
						currentNode = newNode;										// a posuvame dole
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

	private boolean containTurkishChars(String str) {
		return str.matches(".*[ıİşŞğĞüÜöÖçÇ].*");
	}
	
	public static void main() {
		ListParser lp = new ListParser();
		lp.parseListToTree("data/crosby-gazetteer.txt");
	}
}
