package com.nlp.utilities;

import java.io.IOException;
import java.util.Hashtable;

/**
 * TODO Put here a description of what this class does.
 *
 * @author root.
 *         Created Oct 12, 2014.
 */
public class LDAUtilities {
	private boolean removePunc = false;
	private boolean removeStopWords = false;
	private Hashtable<String, String> puncMap = new Hashtable<String, String>();
	private Hashtable<String, Integer> puncs = new Hashtable<String, Integer>();
	private StopWords stopWords;

	/**
	 * TODO Put here a description of what this constructor does.
	 *
	 * @param puncs
	 * @param stopW
	 * @throws IOException
	 */
	public LDAUtilities(boolean puncs, boolean stopW) throws IOException{
		this.stopWords = new StopWords();	
		buildPuncMap();
		this.removePunc = puncs;
		this.removeStopWords = stopW;
	}
	
	private void buildPuncMap(){
		this.puncMap.put("\\.", " . ");
		this.puncMap.put("\\,", " , ");
		this.puncMap.put("\\;", " ; ");
		this.puncMap.put("\\?", " ? ");
		this.puncMap.put("\\!", " ! ");
		this.puncs.put(".", 1);
		this.puncs.put(",", 1);
		this.puncs.put(";", 1);
		this.puncs.put("?", 1);
		this.puncs.put("!", 1);
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param rawDoc
	 * @return cleaned version of doc
	 */
	public String cleanDoc(String rawDoc){
		//remove web link
	    String cleanDoc = rawDoc.replaceAll("\\s*http://\\S+\\s*", " ");
	    //remove numbers
	    cleanDoc = cleanDoc.replaceAll("\\d+", " ");
	    cleanDoc = cleanDoc.toLowerCase();
	    if(this.removeStopWords){
	    		cleanDoc = removeStopWords(cleanDoc);
	    }
		if(this.removePunc){
			cleanDoc = removePuncs(cleanDoc);
		}
	    if(this.removeStopWords){
	    	cleanDoc = removeStopWords(cleanDoc);
	    }
		return cleanDoc;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param text
	 * @return text after removing punctuations
	 */
	public String removePuncs(String text){
		text = text.replaceAll("\\p{Punct}", " ");	 //remove punctuation
	    return text;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param text
	 * @return text after removing stop words
	 */
	public String removeStopWords(String text){
		String [] words = text.split(" ");
		boolean begin = true;
		String cleanText = new String("");
		for(String word:words){
			if(!this.stopWords.containsKey(word) && !word.isEmpty()){
				//System.out.println(word);
				if(begin){
					cleanText += word;
					begin = false;
				}
				else{
					cleanText += (" " + word);
				}
			}
		}
		return cleanText;
	}
}