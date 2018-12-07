package sk.iisas.ikt.gazetteer.pht;

import sk.iisas.ikt.gazetteer.common.PositionLengthHolder;

import org.apache.commons.lang3.time.StopWatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizing chars from input file using gazetteer created in ListParser
 * Author: Giang Nguyen (2013)
 * Author of previous version on FIIT Retrieval course: Adam Pomothy
 */

public class TextAnalyzer {
    private TreeNode rootNode;
    private HashMap<TreeNode, Integer> countHashMap;
    private List<PositionLengthHolder> positionsLengthList;

    public TextAnalyzer() {
        super();
        countHashMap = new HashMap<TreeNode, Integer>();
        positionsLengthList = new ArrayList<PositionLengthHolder>();
    }

    public TextAnalyzer(TreeNode rootNode) {
        this.rootNode = rootNode;
        countHashMap = new HashMap<TreeNode, Integer>();
        positionsLengthList = new ArrayList<PositionLengthHolder>(); 
    }

    //main metoda, pouzita pri merani casovej a pamatovej narocnosti
    public static void main(String[] args) {
    	System.out.println("Java " + System.getProperty("java.runtime.version"));
    	System.out.println("Working directory " + System.getProperty("user.dir"));
    	
    	String parserFile = "data/persons_tripples.txt";
    	String searchFile = "data/_reuters-concat-2586.xml";

    	if (args.length != 2 ){
    	    System.out.println("java -jar gaz.jar searchFile parserFile");
    	} else {
    		searchFile = args[0];
    		parserFile = args[1];
    	}
    	System.out.println("gazetteerCharPHT " + searchFile + " " + parserFile +"\n");
    	
        StopWatch stopwatch = new StopWatch();
        
        System.out.println("------------------- PARSING TO TREE: ");
        stopwatch.start();
        ListParser lp = new ListParser();
        TreeNode rootNode = lp.parseListToTree(parserFile);
        stopwatch.stop();
        System.out.println("Running time (ms) = " + stopwatch.getTime());
        
        System.out.println("------------------- ANALYZING: ");
        stopwatch.reset();
        stopwatch.start();
        TextAnalyzer textAnalyzer = new TextAnalyzer(rootNode);
        textAnalyzer.searchText(searchFile);
        stopwatch.stop();
        System.out.println("Running time (ms) = " + stopwatch.getTime());
                
        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("Used memory (MB) = " + mem/(1024*1024) + "\n");

		System.out.println("-------------------OUTCOME: ");
		int n=0;
		Iterator<Entry<TreeNode, Integer>> it = textAnalyzer.getCountHashMap().entrySet().iterator();
		while (it.hasNext()) {
			n++;
			Map.Entry<TreeNode, Integer> pairs = (Map.Entry<TreeNode, Integer>) it.next();
			TreeNode node = pairs.getKey();
			//System.out.println(node.getEntityName() + " (" + pairs.getValue() + ")\t" + node.getEntityURI());
			//System.out.println(node.getEntityName() + " (" + pairs.getValue() + ")" );
			it.remove(); 				// avoids a ConcurrentModificationException
		}
		System.out.println("----- " + n + " founded entities -----DONE!");
    }

    public void searchText(String filePath) {
        Charset encoding = Charset.forName("UTF-8");
        File file = new File(filePath);
        this.handleFile(file, encoding);
    }

    private void handleFile(File file, Charset encoding) {
        try {
            FileInputStream in			= new FileInputStream(file);
            BufferedReader	reader		= new BufferedReader(new InputStreamReader(in, encoding));
            PushbackReader	pushReader	= new PushbackReader(reader, 1024);      	// will 1024 bytes enough for buffer?
            handleInput(pushReader);
            pushReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInput(PushbackReader reader) {

    	TreeNode currentNode = this.rootNode;
    	String readingString = "";
    	int currentPosition = -1;
    	int charInt;
    	int onNode = 0;   				// values in range <0, currentNode.getcharList().lenght()>

        try {
            while ((charInt = reader.read()) != -1) {			
            		
                char readingChar = Character.toLowerCase((char) charInt);	         
                if (readingChar != '\n' && readingChar != '\r')  	
                	currentPosition++;			
                if (readingChar == ' '  && currentNode.equals(this.rootNode) )	// sme na " " aj zaciname od rootu a tak preskakujeme cyklus	
                	continue;		

                String charList = currentNode.getCharList().toLowerCase();
       			//System.out.println("Pos=" + currentPosition + "\t char=" + readingChar + "\t readingString=" + readingString + "\t onNode=" + onNode + "\t charList=" + currentNode.getCharList() + "\t len=" + currentNode.getCharList().length() + "\t " + currentNode.isListNode() + "\t entityName=" + currentNode.getEntityName());                
                
                if ( (onNode > 0) && (onNode < charList.length()) && (charList.length() > 1) && (readingChar == charList.charAt(onNode)) ){	
                																		// znak je v uzle
                	readingString += readingChar;
               		onNode++;    
               		if ( (onNode == charList.length()) && currentNode.isListNode() ) {			// ak node je list 

               			int nextCharInt = reader.read();									
                        checkMatchedEntity(currentNode, nextCharInt, currentPosition);			// ci nasa entita pokracuje dalej
                        reader.unread(nextCharInt);							
                	}
                } else if (currentNode.getChildNodes().containsKey(readingChar)) {		// znak je prvy znak v child node	
                	           	
                	currentNode = currentNode.getChildNodes().get(readingChar);
                	charList	= currentNode.getCharList().toLowerCase();
                	readingString += readingChar;                	
                	onNode = 1;                  	         			

                	if ( (1 == charList.length()) && currentNode.isListNode() ) {				// a child node je list node, kt. obsahuje readingChar	
                        
                		int nextCharInt = reader.read();										
                        checkMatchedEntity(currentNode, nextCharInt, currentPosition);			// ci nasa entita pokracuje dalej, ak nie, zaznamenajme vyskyt
                        reader.unread(nextCharInt);
                        onNode = 0; 
                	}
                } else {					              							  // znak nie je v strome									
                	// ak sme vnorenim stromom presli cez word's boundaries, 
                	// a nasli sme nejake entity, tak sa treba vratit k prvemu vyskytu boundaries	
                	if ((readingString.length() > 0 ) && containBoundaries(readingString))	{
                	
                    	readingString 	   += readingChar;										// aj ten posledny precitany znak
                    	String[] token		= readingString.split("\\s+", 2);					// kde je prva medzera
                    	String   backString = token[1];											// druhu cast precitaneho stringu dame naspat do bufra
                    	
                    	if (backString.trim().length() > 0) {
                    		char[] backCharArray = backString.toCharArray();
                       		reader.unread(backCharArray);
                       		
                       		//System.out.println("LASTCHAR=" + readingChar + " READING=" + readingString + " BACK=" + backString);
                       		if (readingChar != '\n' && readingChar != '\r')						// kvoli farebnym malovankam
                       				currentPosition = currentPosition - backString.length();
                       		else	currentPosition = currentPosition - backString.length() +1;
                    	}
                    }            
                    currentNode = rootNode;				// ideme na root
                	readingString = "";
                	onNode = 0;
                }
            }
        } catch (Exception e) {
            //logger.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        }
    }   
    
    // pozrieme sa na znak za entitou - ak to nie je pismeno/cislo tak bude matched
    private void checkMatchedEntity(TreeNode currentNode, int nextCharInt, int currentPosition) {
    	  		
        Character nextChar = Character.toLowerCase((char) nextCharInt);
        if (!isDigitLetter(nextChar)) {									// ak nepokracuje tak je to naozaj matched entity		
        	
            if (this.countHashMap.containsKey(currentNode)) {
                Integer currentCount = this.countHashMap.get(currentNode);
                currentCount++;
                this.countHashMap.put(currentNode, currentCount);
            } else {
                this.countHashMap.put(currentNode, new Integer(1));
            }

            PositionLengthHolder plh = new PositionLengthHolder();					// zaznamenam si poziciu a dlzku entity v input file
            plh.length	 = currentNode.getEntityName().length();
            plh.position = currentPosition - plh.length + 1;
            this.positionsLengthList.add(plh);  
            
            //System.out.println("\t MATCHED=" + currentNode.getEntityName());
    	}
    }
    
    // check word's boundaries
    private boolean isDigitLetter(Character ch) {
		if (Character.isDigit(ch) || Character.isLetter(ch))
			 return true;
		else return false;
    }
    
    // also check word's boundaries = white space
    private boolean containBoundaries(String str) {
    	return str.contains(" ");  	
  	/*
    	// better variant for essier extention :)
    	Pattern pattern = Pattern.compile("\\s");
    	Matcher matcher = pattern.matcher(str);
    	return matcher.find();
   	*/
    }
    
/*
    private void removeValueToCountHashMap(TokenTreeNode key) {
        if (this.countHashMap.containsKey(key)) {				
            Integer currentCount = this.countHashMap.get(key);
            if (currentCount > 1) {
                currentCount--;
                this.countHashMap.put(key, currentCount);
            } else {
                this.countHashMap.remove(key);
            }
        }       
    }
*/

    public static double getSizeOfFile(String filePath){
        File file = new File(filePath);
        return (double)file.length()/1024;
    }

    public static String readTextFileToString(String filePath) {
        try {
            FileInputStream fis			= new FileInputStream(filePath);
            BufferedReader	reader		= new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder	strBuilder	= new StringBuilder();
            String currLine = null;
            //find the beginning of the styles section
            while (((currLine = reader.readLine()) != null)) {
                strBuilder.append(currLine);
            }
            reader.close();
            return strBuilder.toString();
            
        } catch (Exception e) {
            //logger.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public TreeNode getRootNode() {        							return rootNode;    }
    public HashMap<TreeNode, Integer> getCountHashMap() {			return countHashMap;    }
    public List<PositionLengthHolder> getPositionsLengthList() {    return positionsLengthList;    }
    
    public void setCountHashMap(HashMap<TreeNode, Integer> countHashMap) {					this.countHashMap = countHashMap;    }
    public void setRootNode(TreeNode rootNode) {        									this.rootNode = rootNode;    }
    public void setPositionsLengthList(List<PositionLengthHolder> positionsLengthList) {	this.positionsLengthList = positionsLengthList;    }
}
