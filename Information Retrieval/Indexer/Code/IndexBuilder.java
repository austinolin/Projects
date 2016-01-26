package B;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
/**
 * Contains the main method as helper methods for running the queries through the models
 * @author Austin
 *
 */
public class IndexBuilder {

    private static File index;
    private static HashMap<String, long[]> byteOffsetCatalog;
    private static double avgDocLength;
    private static double vocabulary;
    private static HashMap<String, Integer> docLengths;
    private static Indexer i;
    private static OtherTools p;
    
    

    /**
     * Will run program. Currently commented out is the steps for building each of the indexes. 
     * If ran as is, it will run the queries on all four indexes and score them, and print them to files
     */
    public static void main(String[] args) {
        p = new OtherTools();
        // no stopping/stemming
        // i = new Indexer(1, p, false, false);
        // buildIndex();

        // stopping
        // i = new Indexer(2, p, true, false);
        // buildIndex();

        // stemming
//        i = new Indexer(3, p, false, true);
//        buildIndex();
//
//        // both
//        i = new Indexer(4, p, true, true);
        //buildIndex();
        
        // Pull info out for index 1 and run queries on it
        updateIndexInfo(1);
        runQueries(1, false, false);
     // Pull info out for index 2 and run queries on it
        updateIndexInfo(2);
        runQueries(2, true, false);
     // Pull info out for index 3 and run queries on it
        updateIndexInfo(3);
        runQueries(3, false, true);
    // Pull info out for index 4 and run queries on it
       updateIndexInfo(4);
       runQueries(4, true, true);
       
       System.out.println("Program finished running.");
    }

    
    
    /**
     * Will run the 25 queries, parse them, score them, and print the results to a file for TF-IDF, Okapi BM25, and LM-JM models
     */
    private static void runQueries(Integer indexType, boolean stop, boolean stem) {
        BufferedReader br = null;
        try {
        	// The file containing the queries
            br = new BufferedReader(new FileReader("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/query_desc.51-100.short.txt"));
            String line;
            Integer counter = 1;
            // Read each query and parse it, then run each set of tokens through the models
            while (((line = br.readLine()) != null) && (counter < 26)) {
                System.out.println("Parsing query " + counter);
                ArrayList<TermAndPosition> tokens = p.parse(line, stop, stem);
                // First token is the query number
                String queryNumber = tokens.get(0).getTermName();
                tokens.remove(0);
                // run rest through models
                runTokens(indexType, queryNumber, tokens);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Will go through a collection of files and sort the documents, and then index them and files related
     * to reading them.
     */
    private static void buildIndex() {
        System.out.println("Parsing files for the documents.");
        File folder = new File("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/ap89_collection");
        // Gets a list of files in the AP89 collection
        File[] files = folder.listFiles();
        // For each file, parse through and get a list of String[2], which contain the docids and the related text
        for (File file : files) {
            ArrayList<String[]> docList = p.getDocuments(file);
            System.out.println(file.getName() + " is in the process of being indexed.");
            // For each document in the list of documents in the file, we will index each one into a temporary index file
            for (String[] doc : docList) {
                i.indexDocument(doc[0], doc[1]);
            }
        }
        // Will merge the index files into a single index file
        i.lastStep();
    }
    
    /**
     *  Will take a list of tokens in a single query, and score them in the models
     */
    private static void runTokens(Integer indexType, String queryNumber, ArrayList<TermAndPosition> queryTerms) {
    	// Each index contains the map of docids to the position lists for the corresponding token in queryTerms
        ArrayList<HashMap<String, ArrayList<Integer>>> queryTermStats = new ArrayList<HashMap<String, ArrayList<Integer>>>();
        // For each term in the queryTerm list, we will retrieve its map from the index and put it into the list
        for (TermAndPosition term : queryTerms) {
            String termName = term.getTermName();
            // if it is in the catalog
            if (byteOffsetCatalog.containsKey(termName)) {
            	// retrieve the byte offset information for the term
	            long[] byteOffset = byteOffsetCatalog.get(termName);
	            // read the map for the term from the index
	            HashMap<String, ArrayList<Integer>> termStats = (HashMap<String, ArrayList<Integer>>) p.read(byteOffset, index);
	            // add it to the list
	            queryTermStats.add(termStats);
            }
        }
        // will calculate the scores for the query terms
        calculateScores(indexType, queryNumber, queryTermStats);

    }

    

    /**
     * Will write the ranked model scores to the correct files
     */
    private static void printResultsToFiles(String queryNumber, SortedSet<Entry<String, Double>> scores, String fileName) {
        Iterator<Entry<String, Double>> i = scores.iterator();
        File outputFile = new File(fileName);
        FileWriter f = null;
        try {
            f = new FileWriter(outputFile, true);
            int scoreRank = 1;
            int scoreCounter = 1;
            while ((scoreCounter < 101) && i.hasNext()) {
                Map.Entry<String, Double> documentScore = i.next();
                String docId = documentScore.getKey();
                double score = documentScore.getValue();
                String posting = queryNumber + " Q0 " + docId + " " + scoreRank + " " + score + " Exp\n";
                f.write(posting);
                scoreRank++;
                scoreCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Will read the indexed files and update the current fields for the index file, the byte offset catalog, the average doc length,
     * the vocabulary size, and the doclengths map
     */
    private static void updateIndexInfo(Integer indexType) {
    	String indexFilePath = "C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/Index" + indexType + ".ser";
        index = new File(indexFilePath);
        byteOffsetCatalog = getCatalog(indexType);
        double[] averageAndVocab = getAverageAndVocab(indexType);
        avgDocLength = averageAndVocab[0];
        vocabulary = averageAndVocab[1];
        docLengths = (HashMap<String, Integer>) getDocLengths(indexType);
    }

    /**
     * Will take a list of Map docid : position list and will pass them into the models, getting scores for them and printing them to
     * an output file
     */
    private static void calculateScores(Integer indexType, String queryNumber, ArrayList<HashMap<String, ArrayList<Integer>>> queryTermStats) {
    	String filename = "C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/ResultsFiles/";
        ScoringModels models = new ScoringModels(queryTermStats, avgDocLength, docLengths);
        SortedSet<Map.Entry<String, Double>> tfIDFScores = models.tfIDF();
        SortedSet<Map.Entry<String, Double>> okapiBM25Scores = models.okapiBM25();
        SortedSet<Map.Entry<String, Double>> lmjmScores = models.lmjm();
         printResultsToFiles(queryNumber, tfIDFScores, filename + "tfIDF" + indexType + ".txt");
         printResultsToFiles(queryNumber, okapiBM25Scores, filename + "okapiBM25" + indexType + ".txt");
         printResultsToFiles(queryNumber, lmjmScores, filename + "lmjm" + indexType + ".txt");

    }
    
    /**
     * Will read the doc lengths file and retrieve the map of doc lengths for the given index
     */
    private static Map<String, Integer> getDocLengths(Integer indexType) {
    	// Initialize streams and file path
        FileInputStream fileInput = null;
        ObjectInputStream objInput = null;
        String docLengthFilePath = "C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/docLengths" + indexType + ".ser";
        Map<String, Integer> readDocLengths = null;
        try {
        	// Read the file, and convert the bytes into the map of doc lengths
        	fileInput = new FileInputStream(docLengthFilePath);
        	objInput = new ObjectInputStream(fileInput);
            readDocLengths = (HashMap<String, Integer>) objInput.readObject();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
            	objInput.close();
                fileInput.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        // return the doc lengths
        return readDocLengths;
    }

    /**
     * Will read a file that contains the average doc length and vocabulary size and return a double[] containing
     * them
     */
    private static double[] getAverageAndVocab(Integer indexType) {
    	// Initialize streams and file path
        FileInputStream fileInput = null;
        ObjectInputStream objInput = null;
        String statisticsFilePath = "C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/avgDocLenAndVocab"  + indexType + ".ser";
        double[] avgDocLengthAndVocab = null;
        try {
        	// Read from file, convert from bytes to a double[]
        	fileInput = new FileInputStream(statisticsFilePath);
            objInput = new ObjectInputStream(fileInput);
            avgDocLengthAndVocab = (double[]) objInput.readObject();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
            	objInput.close();
            	fileInput.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        // return the double[]
        return avgDocLengthAndVocab;
    }

   /**
    * Will read a file and get back a map of the terms to their byte offset information, which is used
    * to find their stats in the index
    */
    private static HashMap<String, long[]> getCatalog(Integer indexType) {
    	// initialize the streams and the file path
        FileInputStream fileInput = null;
        ObjectInputStream objInput = null;
        String catalogFilePath = "C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/byteOffsets" + indexType + ".ser";
        HashMap<String, long[]> catalog = null;
        try {
        	// read the file, convert the bytes into the map
        	fileInput = new FileInputStream(catalogFilePath);
        	objInput = new ObjectInputStream(fileInput);
            catalog = (HashMap<String, long[]>) objInput.readObject();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
            	objInput.close();
                fileInput.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        // return the map
        return catalog;
    }

}
