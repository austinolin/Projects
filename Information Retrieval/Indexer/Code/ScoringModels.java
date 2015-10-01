package B;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
/**
 * Contains the methods for the scoring models
 * @author Austin
 *
 */
public class ScoringModels {

	OtherTools p = new OtherTools();
	// The list of each terms docToPositions maps
    private ArrayList<HashMap<String, ArrayList<Integer>>> queryStats;
    // the map of doclengths
    private HashMap<String, Integer> docLengths;
    // the average doc length
    private double avgDocLength;
    // the number of terms in the collection
    private double COLLECTION_SIZE;


    public ScoringModels(ArrayList<HashMap<String, ArrayList<Integer>>> queryStats, double avgDocLength, HashMap<String, Integer> docLengths) {
        this.queryStats = queryStats;
        this.avgDocLength = avgDocLength;
        this.docLengths = docLengths;
        this.COLLECTION_SIZE = 0;
        // for each document
        for (String docId : docLengths.keySet()) {
        	// add its doc length to sum up the size of the entire collection
            this.COLLECTION_SIZE = COLLECTION_SIZE + docLengths.get(docId);
        }
    }

    /**
     * Will take in a map of documents to docpositions for a term, and return its
     * collection frequency
     */
    public double getCollectionFrequency(HashMap<String, ArrayList<Integer>> termStats) {
    	double collectionFrequency = 0;
    	// For each document
        for (String docId : termStats.keySet()) {
        	// add the term frequency to the sum to get the ttf
        	collectionFrequency = collectionFrequency + (termStats.get(docId)).size();
        }
        return collectionFrequency;
    }

    /**
     * Performs the LM-JM scoring function for each document and puts them in a set
     * @return
     */
    public SortedSet<Map.Entry<String, Double>> lmjm() {
    	//Scores will be docid to score
    	HashMap<String, Double> scores = new HashMap<>();
    	// Get a set of every docid so we can account for the
    	// documents that one or more terms might not be in
        Set<String> completeDocIdMap = compileAllDocIds();
        // for each term in the query
        for (int i = 0; i < queryStats.size(); i++) {
        	// get its doc to positions map
            HashMap<String, ArrayList<Integer>> termStats = queryStats.get(i);
            // get its collection frequency
            double collectionFrequency = getCollectionFrequency(termStats);
            // for each document id in the complete list
            for (String docId : completeDocIdMap) {
                Integer tf = 0;
                // if the term is in it, then change the tf to a value
                if (termStats.containsKey(docId)) {
                    tf = (termStats.get(docId)).size();
                }

                // random lambda value
                double lambda = 0.3;
                double documentLength = docLengths.get(docId);
                double part1 = (tf / documentLength);
                part1 = part1 * lambda;
                double part2 = (1 - lambda);
                part2 = part2 * (collectionFrequency / COLLECTION_SIZE);
                double combined = part1 + part2;
                double lmjm = Math.log(combined);
                // if the docid is already found, add the score to it
                if (scores.containsKey(docId)) {
                    scores.put(docId, scores.get(docId) + lmjm);
                } else {
                	// create new entry for that docid
                    scores.put(docId, lmjm);
                }
            }
        }
        // Order the scores by value
        return p.entriesSortedByValues(scores);
    }
    
    /**
     * Will create a set of all the docids that the query terms can be found in
     */
    private Set<String> compileAllDocIds() {
    	Set<String> completeDocIdMap = new HashSet<String>();
    	// for each term, get its docToPositions map, add the keylist to the complete set
        for (int i = 0; i < queryStats.size(); i++) {
        	HashMap<String, ArrayList<Integer>> termStats = queryStats.get(i);
        	Set<String> docIds = termStats.keySet();
        	completeDocIdMap.addAll(docIds);
        }
        return completeDocIdMap;
    }

    /**
     * Performs the Okapi BM25 model
     */
    public SortedSet<Map.Entry<String, Double>> okapiBM25() {
    	// Scores are docid to score
        HashMap<String, Double> scores = new HashMap<>();
        // for each term in the query
        for (int i = 0; i < queryStats.size(); i++) {
        	// get its docid to positions map
            HashMap<String, ArrayList<Integer>> termStats = queryStats.get(i);
            // the number of documents is doclengths size
            double numOfDocs = docLengths.size();
            double df = termStats.size();
            // for each docid the term is in
            for (String docId : termStats.keySet()) {
                double tf = (termStats.get(docId)).size();
                // if the docid isnt found, put an initial value in
                if (!scores.containsKey(docId)) {
                	scores.put(docId, 0.0);
                }
                double part1 = (numOfDocs + 0.5) / (df + 0.5);
                part1 = Math.log(part1);
                double part21 = (tf + (tf * 1.2));
                double part22 = (tf + (1.2 * (((docLengths.get(docId) / avgDocLength) * 0) + (1 - 0))));
                double part3 = (tf + (tf * .25)) / (.25 + tf);
                double okapibm25 = part1 * (part21 / part22) * part3;
                // just add the score into its spot in the results
                scores.put(docId, scores.get(docId) + okapibm25);
            }
        }
        // order scores by value
        return p.entriesSortedByValues(scores);
    }

    /**
     * Will perform the TF-IDF model
     */
    public SortedSet<Map.Entry<String, Double>> tfIDF() {
    	// Scores are docid to score
        HashMap<String, Double> scores = new HashMap<>();
        // for each term in the query
        for (int i = 0; i < queryStats.size(); i++) {
        	// get its docid to positions map
            HashMap<String, ArrayList<Integer>> termStats = queryStats.get(i);
            // for each document the term is in
            for (String docId : termStats.keySet()) {
                double tf = termStats.get(docId).size();
                double tfidf = (tf / (tf + 0.5 + (1.5 * (docLengths.get(docId) / avgDocLength))));
                tfidf = tfidf * Math.log(docLengths.size() / termStats.size());
                // if the docid is already found, add to it
                if (scores.containsKey(docId)) {
                    scores.put(docId, scores.get(docId) + tfidf);
                } else {
                	//else add new entry
                    scores.put(docId, tfidf);
                }
            }
        }
        // order scores by value
        return p.entriesSortedByValues(scores);
    }
    
    



    
}