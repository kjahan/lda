package com.nlp.lda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;

import com.nlp.utilities.LDAUtilities;

/**
 * TODO Latent Dirichlet Allocation algorithm.
 *
 * @author root.
 *         Created Oct 12, 2014.
 */
public class LDA {
	private double alpha = 0.01;
	private double beta = 0.01;
	private int topicsNum;
	private SparseMatrix docTopicMatrix;
	private SparseMatrix wordTopicMatrix;
	private Map<Integer, Double> NK = new HashMap<Integer, Double>();
	private List<ArrayList<String>> documentMatrix = new ArrayList<ArrayList<String>>();
	private List<ArrayList<Integer>> currentState = new ArrayList<ArrayList<Integer>>();
	private Map<String, Integer> wordIndexMap = new HashMap<String, Integer>();
	private Map<Integer, String> wordIndexInverseMap = new HashMap<Integer, String>();	
	private int vocabSize = 0;
	private LDAUtilities tweetUtil;
	
	/**
	 * TODO LDA constructor.
	 *
	 * @param topicsNum
	 * @param documents
	 * @throws IOException
	 */
	public LDA(int topicsNum, List<String> documents) throws IOException{
		this.topicsNum = topicsNum;
		this.tweetUtil = new LDAUtilities(true, true);
		//build document matrix: assuming we receive a clean version of tweets
		for(String doc : documents){
			ArrayList<String> document = new ArrayList<String>();
			ArrayList<Integer> state = new ArrayList<Integer>();
			//System.out.println(doc);
			String docWoSW = this.tweetUtil.cleanDoc(doc);	//remove stop words
			String tokens[] = docWoSW.split(" ");
			for(String token : tokens){
				document.add(token);
				state.add(-1);
				if(!this.wordIndexMap.containsKey(token)){					
					this.wordIndexMap.put(token, this.vocabSize);
					this.wordIndexInverseMap.put(this.vocabSize, token);
					this.vocabSize++;
				}
			}
			this.documentMatrix.add(document);
			this.currentState.add(state);
		}
		int documentMatrixCardinality[] = {topicsNum, this.documentMatrix.size()};
		this.docTopicMatrix = new SparseMatrix(documentMatrixCardinality);
		int wordTopicMatrixCardinality[] = {this.vocabSize, topicsNum};
		this.wordTopicMatrix = new SparseMatrix(wordTopicMatrixCardinality);
		//init NK
		for(int index = 0; index < topicsNum; index++){
			this.NK.put(index, 0.0);
		}
	}
	
	//debug purpose
	/**
	 * TODO Print docTopic and wordTopic matrixes.
	 *
	 */
	public void printtMatrix(){
		System.out.println("docTopicMatrix:");
		for(int row = 0; row < this.topicsNum; row++){
			for(int col = 0; col < this.documentMatrix.size(); col++){
				System.out.println(this.docTopicMatrix.get(row, col));
			}
		}
		System.out.println("wordTopicMatrix:");
		for(int row = 0; row < this.vocabSize; row++){
			for(int col = 0; col < this.topicsNum; col++){
				System.out.println(this.wordTopicMatrix.get(row, col));
			}
		}
	}
	
	/**
	 * TODO clean matrixes.
	 *
	 */
	public void resetMatrix(){
		for(int row = 0; row < this.topicsNum; row++){
			for(int col = 0; col < this.documentMatrix.size(); col++){
				this.docTopicMatrix.set(row, col, 0.0);
			}
		}
		for(int row = 0; row < this.vocabSize; row++){
			for(int col = 0; col < this.topicsNum; col++){
				this.wordTopicMatrix.set(row, col, 0.0);
			}
		}
	}
	
	//Gibbs Sampler
	private void runGibbsSampler(int docIndex){
		//printtMatrix();
		//System.out.println("doc index=" + docIndex);
		Random rand = new Random();
		ArrayList<String> doc = this.documentMatrix.get(docIndex);
		ArrayList<Integer> state = this.currentState.get(docIndex);
		int index = 0;
		for(String word : doc){
			int zij = state.get(index);	//current assignment for this word
			//System.out.println("current state of word " + word + " --> zij=" + zij);
			//System.out.println("word=" + word);
			int wordIndex = this.wordIndexMap.get(word);
			//System.out.println(word + "'s index = " + wordIndex);
			double u = rand.nextDouble();
			double[] Prob = new double[this.topicsNum + 1];
			Prob[0] = 0.0;
			for(int topicIndex = 1; topicIndex <= this.topicsNum; topicIndex++){
				//System.out.println("topic index=" + (topicIndex - 1));
				double Nkj = this.docTopicMatrix.get(topicIndex - 1, docIndex);
				//System.out.println("Nkj = " + Nkj);				
				double Nwk = this.wordTopicMatrix.get(wordIndex, topicIndex - 1);
				//System.out.println("Nwk = " + Nwk);
				//the following two lines have been changed to address performance issue (by introducing NK ds)
				//Vector vec = wordTopicMatrix.getColumn(topicIndex - 1);
				//double Nk = vec.zSum();
				double Nk = this.NK.get(topicIndex - 1);	//performance issue
				//System.out.println("Nk = " + Nk + ", new NK=" + Nk_new);
				if(zij == (topicIndex - 1)){
					//we need adjustments
					Nkj -= 1.0;
					Nwk -= 1.0;
					Nk -= 1.0;
				}
				Prob[topicIndex] = Prob[topicIndex - 1] + (Nkj + this.alpha)*(Nwk + this.beta)/(Nk + this.vocabSize*this.beta);
			}
			/*
			System.out.println("u=" + u);
			System.out.println("probability:");
			for(int topicIndex = 1; topicIndex <= topicsNum; topicIndex++){
				System.out.println(Prob[topicIndex]/Prob[topicsNum]);
			}
			*/
			for(int topicIndex = 1; topicIndex <= this.topicsNum; topicIndex++){
				if(u <= Prob[topicIndex]/Prob[this.topicsNum]){
					state.set(index, topicIndex - 1);
					//update Nkj, Nwk, and Nk
					double newNkj = this.docTopicMatrix.get(topicIndex - 1, docIndex);
					double newNwk = this.wordTopicMatrix.get(wordIndex, topicIndex - 1);
					double newNk = this.NK.get(topicIndex - 1);
					if(zij != (topicIndex - 1)){
						this.docTopicMatrix.set(topicIndex - 1, docIndex, (newNkj + 1.0));
						this.wordTopicMatrix.set(wordIndex, topicIndex - 1, (newNwk + 1.0));
						this.NK.put(topicIndex - 1, (newNk + 1.0));
						if(zij >= 0){
							double oldNkj = this.docTopicMatrix.get(zij, docIndex);
							double oldNwk = this.wordTopicMatrix.get(wordIndex, zij);
							double oldNk = this.NK.get(zij);
							this.docTopicMatrix.set(zij, docIndex, (oldNkj - 1.0));
							this.wordTopicMatrix.set(wordIndex, zij, (oldNwk - 1.0));
							this.NK.put(zij, (oldNk - 1.0));
						}
					}					
					break;	//done with this word
				}
			}
			index++;
		}
		/*
		System.out.println("state");
		for(int s : state){
			System.out.println(s);
		}
		*/
		this.currentState.set(docIndex, state);
	}
	
	/**
	 * TODO get TopicWord probability.
	 *
	 * @param word
	 * @param topicIndex
	 * @return P(k|w)
	 */
	public double getTopicWordProbability(String word, int topicIndex){
		int wordIndex = this.wordIndexMap.get(word);
		double Nwk = this.wordTopicMatrix.get(wordIndex, topicIndex);
		Vector vec = this.wordTopicMatrix.getColumn(topicIndex);
		double Nk = vec.zSum();
		
		return (Nwk + this.beta)/(Nk + this.vocabSize*this.beta);
	}
	
	/**
	 * TODO get TopicDoc probability.
	 *
	 * @param docIndex
	 * @param topicIndex
	 * @return P(k|d)
	 */
	public double getTopicDocProbability(int docIndex, int topicIndex){
		double Nkj = this.docTopicMatrix.get(topicIndex, docIndex);
		Vector vec = this.docTopicMatrix.getColumn(docIndex);
		double Nj = vec.zSum();
		
		System.out.println("Nkj=" + Nkj);
		System.out.println("Nj=" + Nj);
		
		return (Nkj + this.alpha)/(Nj + this.topicsNum*this.alpha);
	}
	
	/**
	 * TODO Print top words from a topic word distribution.
	 *
	 * @param topicIndex
	 * @param top
	 */
	public void printTopicTopWords(int topicIndex, int top){
		Vector vec = this.wordTopicMatrix.getColumn(topicIndex);
		int indexes[] = new int[top];
		Hashtable<Integer, Integer> indexMap = new Hashtable<Integer, Integer>();
		for(int index = 0; index < top; index++){
			int maxInx = -1;
			double max = 0.0;
			//System.out.println("max=" + max + ", maxInx=" + maxInx);
			for(int wordInx = 0; wordInx < vec.size(); wordInx++){
				//System.out.println("wordInx=" + wordInx + ", count=" + vec.get(wordInx));
				if(vec.get(wordInx) > max && !indexMap.containsKey(wordInx)){
					maxInx = wordInx;
					max = vec.get(wordInx);
				}
			}
			//System.out.println("top word#" + index);
			//System.out.println("top word index=" + maxInx);
			if(maxInx >= 0){
				indexMap.put(maxInx, 0);
				indexes[index] = maxInx;
			}
		}
		/*
		System.out.println("word index map:");
		for(int index = 0; index < vocabSize; index++){
			System.out.println(index + ":" + wordIndexInverseMap.get(index));
		}
		*/
		System.out.println("Top words for topic=" + topicIndex);
		for(int index = 0; index < top; index++){
			if(vec.get(indexes[index]) > 0){
				System.out.println(this.wordIndexInverseMap.get(indexes[index]) + ":" + getTopicWordProbability(this.wordIndexInverseMap.get(indexes[index]), topicIndex));
			}
		}
	}
	
	/**
	 * TODO method for running LDA for times iterations.
	 *
	 * @param times
	 */
	public void runLDA(int times){
		resetMatrix();
		for(int iteration = 1; iteration <= times; iteration++){
			for(int docIndex = 0; docIndex < this.documentMatrix.size(); docIndex++){
				runGibbsSampler(docIndex);
			}
			//System.out.println("iteration #" + iteration);
			//printtMatrix();
		}
	}
}