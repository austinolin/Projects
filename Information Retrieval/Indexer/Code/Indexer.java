package B;

import java.io.*;
import java.util.*;

/**
 * Class that will handle the indexing of documents into the index.
 * @author Austin
 *
 */
public class Indexer {

   
    private File temp;
    private File index;
    private File byteOffsetsFile;
    private File docLengthsFile;
    private File avgDocLenAndVocabFile;
    private OtherTools p;
    private HashMap<String, DocToPositionsMap> termMaps;
    private HashMap<String, Integer> docLengths;
    private HashMap<String, ArrayList<long[]>> tempByteOffsets;
    private HashMap<String, long[]> byteOffsetCatalog;
    private boolean stem;
    private boolean stop;


    public Indexer(Integer indexNumber, OtherTools p, boolean stop, boolean stem) {
        this.p = p;
        this.termMaps = new HashMap<String, DocToPositionsMap>();
        this.tempByteOffsets = new HashMap<String, ArrayList<long[]>>();
        this.docLengths = new HashMap<String, Integer>();
        this.byteOffsetCatalog = new HashMap<String, long[]>();
        this.stop = stop;
        this.stem = stem;

        this.temp = new File("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/tempIndex" + indexNumber + ".ser");
        this.index = new File("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/finalIndex" + indexNumber + ".ser");
        this.docLengthsFile = new File("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/docLengths" + indexNumber + ".ser");
        this.avgDocLenAndVocabFile = new File("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/avgDocLenAndVocab" + indexNumber
                + ".ser");
        this.byteOffsetsFile = new File("C:/Users/Austin/Desktop/elasticsearch-1.4.2/AP89_DATA/AP_DATA/HW2/IndexFiles/byteOffsets" + indexNumber + ".ser");
        try {
        	// Will go through and if there is already a file matching any of the field files, it will delete it and create a new one
            temp.delete();
            temp.createNewFile();
            byteOffsetsFile.delete();
            byteOffsetsFile.createNewFile();
            index.delete();
            index.createNewFile();
            docLengthsFile.delete();
            docLengthsFile.createNewFile();
            avgDocLenAndVocabFile.delete();
            avgDocLenAndVocabFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Will take in a document id and the text and will parse it. Then it will index it to a
     * temporary file
     */
    public void indexDocument(String docId, String text) {
    	// Parse for stopwords or stem, as well as perform the regex
        ArrayList<TermAndPosition> termList = p.parse(text, stop, stem);
        // Add the doclength to the doclengths map
        docLengths.put(docId, termList.size());
        // for every term in the document, we will index it
        for (TermAndPosition term : termList) {
            indexTerm(docId, term.getTermName(), term.getPosition());
        }
    	
    }

    /**
     * Will take in a doc id, the term, and its position in the document, and index add it to
     * our term map. when the term map gets to 1000, it will write it to a file and clear itself
     */
    public void indexTerm(String docId, String termName, Integer position) {
    	// If we are at 1000 terms in our map, then we need to index those into a file
        if (termMaps.size() > 999) {
        	// For each term, we want to get its docToPositions map, serialize it and get its byte offset info,
        	// then add that to our map of term to its list of byte offsets
        	for (String term : termMaps.keySet()) {
                Map<String, ArrayList<Integer>> docToPositions = (termMaps.get(term)).getDocToPositionsMap();
                long[] byteOffsetInfo = p.write(p.serialize(docToPositions), temp); //
                if (tempByteOffsets.containsKey(term)) {
                	// update the term to have this byte offset added to its list
                	ArrayList<long[]> updated = tempByteOffsets.get(term);
                	updated.add(byteOffsetInfo);
                	tempByteOffsets.put(term, updated);
                } else {
                	// add term and this byte offset
                	ArrayList<long[]> updated = new ArrayList<long[]>();
                	updated.add(byteOffsetInfo);
                	tempByteOffsets.put(term, updated);
                }
            }
            termMaps.clear();
        	
        } else {
        	// We have less than 1000 terms in the term map, so we just add it to the term map
            if (termMaps.containsKey(termName)) {
            	// update the term's docToPositions map
            	DocToPositionsMap updated = (termMaps.get(termName)).addPosition(docId, position);
                termMaps.put(termName, updated);
            } else {
            	// create a new docToPositionsMap for the term, then add it to the term map
            	DocToPositionsMap updated = (new DocToPositionsMap()).addPosition(docId, position);
                termMaps.put(termName, updated);
            }
        }
    }

   /**
    * Will make sure to index any terms still unindexed from the term map, then merge the temp index into a final index
    * Will also serialize the byte offset catalog, the doclengths, and the stats on average doc length and vocab size
    */
    public void lastStep() {
    	for (String term : termMaps.keySet()) {
    		Map<String, ArrayList<Integer>> docToPositions = (termMaps.get(term)).getDocToPositionsMap();
            long[] byteOffsetInfo = p.write(p.serialize(docToPositions), temp); //
            if (tempByteOffsets.containsKey(term)) {
            	ArrayList<long[]> updated = tempByteOffsets.get(term);
            	updated.add(byteOffsetInfo);
            	tempByteOffsets.put(term, updated);
            } else {
            	ArrayList<long[]> updated = new ArrayList<long[]>();
            	updated.add(byteOffsetInfo);
            	tempByteOffsets.put(term, updated);
            }
        }
        termMaps.clear();
        System.out.println("Merging into final index");
        mergePartialIndexes();
        p.write(p.serialize(byteOffsetCatalog), byteOffsetsFile);//
        p.write(p.serialize(docLengths), docLengthsFile);//
        writeCollectionStats();
        System.out.println("FINISHED.");
    }

    /**
     * Will compute the average doc length and the vocabulary size and
     * put them into a double[]
     */
    private void writeCollectionStats() {
        double avgDocLength = 0;
        // for each doc in the doclengths, it will sum up the lengths
        for (String docId : docLengths.keySet()) {
            avgDocLength += docLengths.get(docId);
        }
        // then divide by the number of documents to get the average doc length
        avgDocLength /= docLengths.size();
        // The number of terms in the catalog (unique) is the vocab size
        double vocabSize = byteOffsetCatalog.size();
        double[] avgDocLenAndVocab = { avgDocLength, vocabSize };
        // Serialize them in a file for use later
        p.write(p.serialize(avgDocLenAndVocab), this.avgDocLenAndVocabFile);

    }

   /**
    * Will merge all of the common terms in the temp index and output them to a final index file
    */
    public void mergePartialIndexes() {
    	
        ArrayList<long[]> byteOffsets = null;
        // Will combine the term docToPositions into this map, then index it
        // does this for each term
        HashMap<String, ArrayList<Integer>> results = null;
        System.out.println("Terms to process: " + tempByteOffsets.size());

        int counter = 1;
        for (String term : tempByteOffsets.keySet()) {
            System.out.println("Merging term " + counter + ": " + term);
            // Get the list of byte offsets for the term
            byteOffsets = tempByteOffsets.get(term);
            results = new HashMap<String, ArrayList<Integer>>();

            HashMap<String, ArrayList<Integer>> currentTermMap = null;
            // Iterate through the list of byte offsets and add each resulting
            // docToPositions map's contents to the results
            for (long[] byteOffset : byteOffsets) {
                currentTermMap = (HashMap<String, ArrayList<Integer>>) p.read(byteOffset, temp);//
                results = addAll(results, currentTermMap);
            }
            // Serialize the results term map and then add the byte offset info to
            // the final byte offset catalog
            long[] resultByteInfo = p.write(p.serialize(results), index);//
            byteOffsetCatalog.put(term, resultByteInfo);
            counter++;
        }
    }

    /**
     * Will add all of the contents of the current map to the results map, and return the results map
     */
    private HashMap<String, ArrayList<Integer>> addAll(HashMap<String, ArrayList<Integer>> results, HashMap<String, ArrayList<Integer>> current) {
        ArrayList<Integer> newPositions = null;
        ArrayList<Integer> oldPositions = null;
        // For each document in the current term map
        for (String docId : current.keySet()) {
        	// if the docid is already found in the results, add all of the positions of 
        	// current to the results positions
            if (results.containsKey(docId)) {
                newPositions = results.get(docId);
                oldPositions = current.get(docId);
                newPositions.addAll(oldPositions);
                results.put(docId, newPositions);
            } else {
            	// otherwise just put the doc id and the positions in results
                results.put(docId, current.get(docId));
            }
        }
        // return results
        return results;
    }




}