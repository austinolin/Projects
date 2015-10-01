import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;













import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.io.IOUtils;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import crawlercommons.robots.SimpleRobotRulesParser;

// Get robots.txt, find third party thing
// Figure out inlinks
// Cleaned text index with positions
// Index to elasticsearch

public class Crawler {
	private static Client client;
	private static Frontier frontier = null;
	private static int iteration = 0;

	

	public Crawler() throws IOException {
		Node node = nodeBuilder().client(true).clusterName("clarke").node();
		client = node.client();
		//client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9200));
		frontier = new Frontier();
//		FrontierItem seed1 = new FrontierItem("http://www.saveur.com/content/best-food-blog-awards-2014-winners", true, System.currentTimeMillis());
//		frontier.add(seed1);
//
//		FrontierItem seed2 = new FrontierItem("https://www.foodblogs.com/", true, System.currentTimeMillis());
//		frontier.add(seed2);
		
		//"http://www.refinery29.com/food-blogs"
//		FrontierItem seed3 = new FrontierItem("http://foodgawker.com/", true, System.currentTimeMillis());
//		frontier.add(seed3);
		FrontierItem seed4 = new FrontierItem("http://www.refinery29.com/food-blogs", true, System.currentTimeMillis());
		frontier.add(seed4);

		
		processURLs();
//		while (!frontier.isEmpty()) {
//			System.out.println(frontier.getNext().prettyPrint());
//		}
		Map<String, FrontierItem> crawled = frontier.getCrawledMap();
//		FrontierItem seedFinal1 = crawled.get("http://www.saveur.com/content/best-food-blog-awards-2014-winners");
//		System.out.println(seedFinal1.prettyPrint());
//		FrontierItem seedFinal2 = crawled.get("https://www.foodblogs.com/");
//		System.out.println(seedFinal2.prettyPrint());
//		FrontierItem seedFinal3 = crawled.get("http://foodgawker.com/");
//		System.out.println(seedFinal3.prettyPrint());
//		System.out.println(crawled.keySet().size());
		Integer counter = 1;
		for (String url : crawled.keySet()) {
			FrontierItem fi = crawled.get(url);
			System.out.println("Updating " + counter);
			updateInlinks(fi);
			counter++;
		}
		System.out.println("Finished with everything");
	}
	

	
	
	public static void processURLs() throws IOException {
		while (!frontier.isEmpty() && frontier.getCrawledMap().keySet().size() <= 4000) {			
			// Gets the URL, the html, the text, and the outlinks
			FrontierItem nextUrl = frontier.getNext();
//			System.out.println(nextUrl.prettyPrint());
			String _id = nextUrl.getURL();
			//System.out.println("Processing URL: " + _id);
			Object[] ccInfo = getRobotsInfo(_id);
			if ((boolean) ccInfo[0] == false) {
				continue;
			}
//			if ((_id.contains("pinterest.com")) || (_id.contains("twitter.com"))) {
//				continue;
//			}
			long rest = 1000;
			if ((long) ccInfo[1] > 1000) {
				rest = (long) ccInfo[1];
			}
			try {
			Document page = Jsoup.connect(_id).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36").get();
			iteration++;
			System.out.println(iteration);
			String html = page.outerHtml(); 
			String text = page.body().text();
			ArrayList<String> outlinks = new ArrayList<String>();
			
			// Get all outlinks
			Elements outlinksList = page.select("a[href]");
			for(Element link: outlinksList){
				// Add string URL to outlinks 
				String outlink = link.attr("abs:href");
				if (outlink.equals("")) {
	                continue;
	            }
				outlinks.add(canonicalize(outlink));
				FrontierItem addedURL = new FrontierItem(outlink, false, System.currentTimeMillis());
				addedURL.addInlink(_id);
				frontier.add(addedURL);				
			}
			HashMap<String, Object> urlInfo = new HashMap<String, Object>();
			ArrayList<String> emptyInlinks = new ArrayList<String>();
			urlInfo.put("url", _id);

			urlInfo.put("html", html);
			urlInfo.put("text", text);
			urlInfo.put("out-links", outlinks.toArray());
			urlInfo.put("in-links", emptyInlinks.toArray());
			buildIndex(urlInfo);
			} 
			catch(IOException e) {
				System.out.println("Dealt with:" + e);
			}
			
			catch(IllegalArgumentException s) {
				System.out.println("Dealt with:" + s);
			}
			
			try {
			    Thread.sleep(rest);                 //1000 milliseconds is one second.
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}


	/**
	 * Will take a URL and canonicalize it
	 */
	public static String canonicalize(String urlString) {
        String result = null;
        try {
            URL url = new URL(urlString.toLowerCase());
            result = url.getProtocol() + "://" + url.getHost() + url.getPath().replaceAll("/+", "/").replaceFirst("/", "//");;
        } catch (MalformedURLException e) {
            System.out.println("Malformed url: " + urlString);
        }
        return result;
    }
	
	/**
     * Builds the index. 
     */
    private static void buildIndex(HashMap<String, Object> urlInfo) {
    	
    	String url = (String) urlInfo.get("url");
        client.prepareIndex(
        		"crawler", "document",
        		url)
                .setSource(urlInfo).execute().actionGet();
    }
    
    /**
     * Will update the inlinks in the index
     * @param url
     */
    private static void updateInlinks(FrontierItem url) {
    	UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("crawler");
        updateRequest.type("document");
        updateRequest.id(url.getURL());
        try {
            updateRequest.doc(XContentFactory.jsonBuilder()
                    .startObject()
                        .field("in-links", url.getInlinks().toArray())
                    .endObject());
            client.update(updateRequest).get();
        } catch (IOException e) {
            System.out.println("Failed to create update for: " + url.getURL());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    

    // CRAWLER COMMONS CODE
    
    public static Object[] getRobotsInfo(String url) {
		Object[] ccInfo = new Object[2];
		boolean isItCrawlable = isCrawlable(url, "Googlebot");
		//System.out.println("Crawlable URL: " + isItCrawlable);
		// IN MILLI SECONDS
		long crawlDelay = getCrawlDelay(url, "Googlebot");
		//System.out.println("Delay: " + crawlDelay);
		ccInfo[0] = isItCrawlable;
		ccInfo[1] = crawlDelay;
		return ccInfo;
	}

	public static long getCrawlDelay(String page_url, String user_agent) {
		try {
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			//System.out.println(hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							IOUtils.toByteArray(robotsContent), "text/plain",
							user_agent);
				}
			}
			return rules.getCrawlDelay();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public static boolean isCrawlable(String page_url, String user_agent) {
		try {
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			//System.out.println(hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							IOUtils.toByteArray(robotsContent), "text/plain",
							user_agent);
				}
			}
			return rules.isAllowed(page_url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getContents(String page_url) {
		InputStream is = null;
		try {
			URLConnection openConnection = new URL(page_url).openConnection();
			openConnection
					.addRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			is = openConnection.getInputStream();
			String theString = IOUtils.toString(is);
			return theString;
		} catch (MalformedURLException e) {
			System.out.println("IoException");
			//e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IoException");
		}
		
		
		return null;
	}
	
}