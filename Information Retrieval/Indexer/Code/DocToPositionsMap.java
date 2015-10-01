package B;

import java.util.*;
/**
 * Will handle the documents to position list map to make it easier to edit
 * @author Austin
 *
 */

public class DocToPositionsMap {

    private HashMap<String, ArrayList<Integer>> docToPositionsMap; // HashMap<docId:ListOfPositions>

    public DocToPositionsMap() {
    	docToPositionsMap = new HashMap<String, ArrayList<Integer>>();
    }


    /**
     * Will add a position to the given doc id for a term if it exists already,
     * or will add a new doc id with that position if not
     */
    public DocToPositionsMap addPosition(String docId, Integer position) {
        if (docToPositionsMap.containsKey(docId)) {
            ArrayList<Integer> positions = docToPositionsMap.get(docId);
            positions.add(position);
            docToPositionsMap.put(docId, positions);
        } else {
            ArrayList<Integer> positions = new ArrayList<Integer>();
            positions.add(position);
            docToPositionsMap.put(docId, positions);
        }
        return this;
    }

    /**
     * Will return the map of docid to position list
     */
    public HashMap<String, ArrayList<Integer>> getDocToPositionsMap() {
        return this.docToPositionsMap;
    }
}
    
