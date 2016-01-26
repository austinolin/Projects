import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class PageRankCalculator {

	static Map<String, ArrayList<String>> docsToInlinks = new HashMap<String, ArrayList<String>>();
	static Map<String, Integer> docsToOutlinks = new HashMap<String, Integer>();
	static Map<String, Double> pageRanks = new HashMap<String, Double>();
	
	
	public static void main(String[] args) throws IOException {
		String input = "C:/Users/Austin/workspace/HW3/wt2g_inlinks.txt";
		String output = "C:/Users/Austin/workspace/HW3/pagerank.txt";
//		String crawledInput = "C:/Users/Austin/workspace/HW3/LinkGraph";
//		String crawledOutput = "C:/Users/Austin/workspace/HW3/crawled_pagerank.txt";
		FileWriter writer = new FileWriter(output);
		
		BufferedReader in = new BufferedReader(new FileReader(input));
//		BufferedReader crawled = new BufferedReader(new FileReader(crawledInput));
		String line;
		
		// Will go through, add the docid and inlinks list to map, update tally of outlink count for each
		// inlink
		while (((line = in.readLine()) != null) && (line.length() != 0)) {
			ArrayList<String> inlinks = new ArrayList<String>();
			String[] splitLine = line.split("\\s+");
			String docid = splitLine[0];
			docid = docid.trim();
			if (!docsToOutlinks.containsKey(docid)) {
				docsToOutlinks.put(docid, 0);
			}
			if (splitLine.length > 1) {
				for (int i = 1; i < splitLine.length; i++) {
					inlinks.add(splitLine[i].trim());
					if (!docsToOutlinks.containsKey(splitLine[i].trim())) {
						docsToOutlinks.put(splitLine[i].trim(), 1);
					} else {
						docsToOutlinks.put(splitLine[i].trim(), docsToOutlinks.get(splitLine[i].trim()) + 1);
					}
				}
			}
			docsToInlinks.put(docid, inlinks);
		}
		System.out.println("Check");
		boolean flag = false;
		while (!flag) {
			for (int i = 0; i < 75; i++) {
				//System.out.println(i);
				for (String docid : docsToInlinks.keySet()) {
					Double old = null;
					if (pageRanks.containsKey(docid)) {
						old = pageRanks.get(docid);
					}
					//System.out.println(old);
					ArrayList<String> inlinks = docsToInlinks.get(docid);
					double newPR = (1 - .85);
					if (inlinks.isEmpty()) {
						pageRanks.put(docid, newPR);
					} else {
						double part1 = .85;
						double part2 = 0;
						for (int j = 0; j < inlinks.size(); j++) {
							String inlink = inlinks.get(j);
							double inlinkPR = .85;
							if (pageRanks.containsKey(inlink)) {
								inlinkPR = pageRanks.get(inlink);
							}
							Integer outlinkCount = docsToOutlinks.get(inlink);
							//System.out.println(outlinkCount);
							if (outlinkCount < 1) {
								
							}
							part2 = part2 + (inlinkPR/outlinkCount);	
						}
						newPR = newPR + (part1 * part2);
						pageRanks.put(docid, newPR);
					}
					if ((old != null) && (((newPR - old) / docsToInlinks.size()) < .1)) {
						//System.out.println(((newPR - old) / docsToInlinks.size()));
						flag = true;	
					}
					
				}
			}
			
		}
		SortedSet<Map.Entry<String, Double>> sortPageRanks = entriesSortedByValues(pageRanks);
		for (Map.Entry<String, Double> pair : sortPageRanks) {
			System.out.println(pair.getKey() + ", Pagerank = " + pair.getValue() + "\n");
			writer.write(pair.getKey() + ", Pagerank = " + pair.getValue() + "\n");
		}
		
		
	}
	  // Method found on StackOverflow that sorts Hashmaps by their values
    // http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
            Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue()) * -1;
                        return res != 0 ? res : 1;
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
