import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Frontier {

	private static FrontierComparator fc = new FrontierComparator();
	@SuppressWarnings("unchecked")
	private static PriorityQueue<FrontierItem> frontier;// = new PriorityQueue<FrontierItem> (11, fc);
	private static Map<String, FrontierItem> seen = new HashMap<String, FrontierItem>();
	private static Map<String, FrontierItem> crawled = new HashMap<String, FrontierItem>();
	
	public Frontier() {
		frontier = new PriorityQueue<FrontierItem>(new Comparator<FrontierItem>() {
            @Override
            public int compare(FrontierItem url1, FrontierItem url2) {
                if (url1.isSeed()) {
                    return -1;
                } else if (url2.isSeed()) {
                    return 1;
                }

                int res = url1.getInlinkCount().compareTo(url2.getInlinkCount())
                        * -1;

                if (res == 0) {
                    res = url1.getTimeCreated().compareTo(url2.getTimeCreated());
                }
                return res;
            }
        });
	}
	
	/**
	 * @return true if frontier is empty, else false
	 */
	public boolean isEmpty() {
		return frontier.isEmpty();
	}
	
	public PriorityQueue<FrontierItem> getFrontierQueue() {
		return frontier;
	}
	/**
	 * returns true if the URL has already been crawled or put in the frontier
	 */
	public boolean isSeen(FrontierItem url) {
		return seen.containsKey(url.getURL()); // Might need to canonicalize url
	}
	
	/**
	 * returns true if the URL is currently in the Frontier
	 */
	public boolean inFrontier(FrontierItem url) {
		return frontier.contains(url);
	}
	
	
	/**
	 * Will add a frontieritem URL into the frontier if it is not crawled or in the frontier already
	 */
	public void add(FrontierItem url) {
		if ((!isSeen(url)) && (!inFrontier(url))) {
			frontier.add(url);
			seen.put(url.getURL(), url);
		} else {
			updateInlinksFor(url);
		}
	}
	
	/**
	 * @return the Map of all seen URLs
	 */
	public Map<String, FrontierItem> getSeenMap() {
		return seen;
	}
	
	
	
	
	
	/**
	 * Will update the inlink count for a url that is already seen
	 */
	public void updateInlinksFor(FrontierItem url) {
		
		// pull out the already seen URL
		FrontierItem updated = seen.get(url.getURL());
		// Pull out the list of new inlinks to be added
		ArrayList<String> newInlinks = url.getInlinks();
		// for each inlink to be added, add to the seen inlinks if not already contained
		for (int i = 0; i < newInlinks.size(); i++) {
			if (!(updated.getInlinks()).contains(newInlinks.get(i))) {
				updated.addInlink(newInlinks.get(i));
			}
		}
		// update the FrontierItem for that URL in our seen map
		seen.put(updated.getURL(), updated);
		if (crawled.containsKey(updated.getURL())) {
			crawled.put(updated.getURL(), updated);
		}
		// update the frontier if the URL is in it
		if (frontier.contains(updated)) {
			frontier.remove(updated);
			frontier.add(updated);
		}
		
	}
	
	public Map<String, FrontierItem> getCrawledMap() {
		return crawled;
	}
	public FrontierItem getNext() {
		FrontierItem top = frontier.poll();
		crawled.put(top.getURL(), top);
		return top;
	}
	

}
