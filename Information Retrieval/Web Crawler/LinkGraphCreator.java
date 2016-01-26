import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;


public class LinkGraphCreator {


	  private static int counter = 0;
	    private static String index1Name = "crawler_austin";

	    public static void main(String args[]) throws InterruptedException {

//	        Settings settings = ImmutableSettings.settingsBuilder()
//	                .put("cluster.name", "clarke").build();
//	        Client client = new TransportClient(settings)
//	        .addTransportAddress(new InetSocketTransportAddress(
//	                "10.0.0.12", 9300));
	    	Node node = nodeBuilder().client(true).clusterName("clarke").node();
	 		Client client = node.client();

	        SearchResponse scrollResp = client.prepareSearch(index1Name)
	                .setSearchType(SearchType.QUERY_THEN_FETCH)
	                .setScroll(new TimeValue(60000))
	                .setQuery(QueryBuilders.matchAllQuery()).setSize(100).execute()
	                .actionGet();

	        System.out.println("Number of hits: "
	                + scrollResp.getHits().getTotalHits());

	        while (true) {
	        	File outputFile = new File("C:/Users/Austin/Desktop/linkGraph.txt");
	            FileWriter f = null;
	            try {
					f = new FileWriter(outputFile, true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            for (SearchHit hit : scrollResp.getHits().getHits()) {
	            	counter++;
	            	String id = hit.getId();
	            	String line = id;
	            	ArrayList<String> outlinks = (ArrayList<String>) hit.getSource().get("out-links");
	            	for(int i = 0; i < outlinks.size(); i++) {
	            		line = line + "\t" + outlinks.get(i);
	            	}
	            	line = line + "\n\n";
	            	try {
						f.write(line);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	
	                
	            }

	            System.out.println("Documents processed: " + counter);

	            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
	                    .setScroll(new TimeValue(60000)).execute().actionGet();

	            if (scrollResp.getHits().getHits().length == 0) {
	                break;
	            }
	        }

	        System.out.println("Finished writing Link Graph");
	        client.close();
	    }
}
