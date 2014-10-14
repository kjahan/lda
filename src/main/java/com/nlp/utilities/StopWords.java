package com.nlp.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import com.nlp.utilities.Constants;

/**
 * TODO Stop words class.
 *
 * @author root.
 *         Created Oct 13, 2014.
 */
public class StopWords {
	private Hashtable<String, Integer> stopWords = new Hashtable<String, Integer >();
	
	/**
	 * TODO Put here a description of what this constructor does.
	 *
	 * @throws IOException
	 */
	public StopWords() throws IOException{
		parseStopWords();
	}
	
	private int parseStopWords() throws IOException{
		String word;
		BufferedReader in = new BufferedReader(new FileReader(Constants.STOP_WORDS_FILE_NAME));
		while ((word = in.readLine()) != null) {
			this.stopWords.put(word, 1);
		}
		in.close();
		return 0;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param word
	 * @return boolean
	 */
	public boolean containsKey(String word){
		if(!this.stopWords.containsKey(word)){
			return false;
		}
		return true;
	}
}