import java.net.MalformedURLException;

import java.net.URL;
import java.util.ArrayList;


public class FrontierItem {

	private String url;
	private boolean isSeed;
	private ArrayList<String> inlinks = null;
	private long timeCreated;
	
	public FrontierItem(String url, boolean isSeed, long timeCreated) throws MalformedURLException {
		this.url = canonicalize(url);
		this.isSeed = isSeed;
		this.timeCreated = timeCreated;
		this.inlinks = new ArrayList<String>();
	}
	
	/**
	 * Will add a new url inlink to this url's list of inlinks if it isn't already
	 * contained in the list
	 * @param url
	 */
	public void addInlink(String url) {
		if (!inlinks.contains(url)) {
			inlinks.add(url);
		}
	}
	
	/**
	 * Will return the list of inlink urls for this url
	 */
	public ArrayList<String> getInlinks() {
		return inlinks;
	}
	
	/**
	 * Will return the count of inlinks this url has
	 */
	public Integer getInlinkCount() {
		return inlinks.size();
	}
	
	/**
	 * returns the URL string
	 */
	public String getURL() {
		return url;
	}
	
	/**
	 * returns true if this URL is one of the seed URLs, else false
	 */
	public boolean isSeed() {
		return isSeed;
	}
	
	/**
	 * Will take a URL and canonicalize it
	 */
	public static String canonicalize(String urlString) {
        String result = null;
        try {
            URL url = new URL(urlString.toLowerCase());
            result = url.getProtocol() + "://" + url.getHost() + url.getPath();
        } catch (MalformedURLException e) {
            System.out.println("Malformed url: " + urlString);
        }
        return result;
    }

	/**
	 * Returns which timestamp the FrontierItem was put into the frontier
	 */
	public Long getTimeCreated() {
		return timeCreated;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	 @Override
	    public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if (getClass() != obj.getClass())
	            return false;
	        FrontierItem other = (FrontierItem) obj;
	        if (url == null) {
	            if (other.url != null)
	                return false;
	        } else if (!url.equals(other.url))
	            return false;
	        return true;
	    }
	
	 public String prettyPrint() {
	        return "Url: " + url + "\n" + "Time crawled: " + timeCreated + "\n"
	                + "Seed? " + isSeed + "\n" + "Number of inLinks: "
	                + inlinks.size() + "\n";// + printInLinks() + "\n";
	    }
	 
	 private String printInLinks() {
	        String result = "";

	        int counter = 1;
	        for (String url : inlinks) {
	            result += "InLinks " + counter + ": " + url + "\n";
	            counter++;
	        }

	        return result;
	    }

	
	
}
