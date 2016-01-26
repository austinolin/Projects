import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class MergeIndex {

    private static int counter = 0;
    private static String index1Name = "crawler";
    private static String index2Name = "crawler_austin";

    public static void main(String args[]) throws InterruptedException {

//        Settings settings = ImmutableSettings.settingsBuilder()
//                .put("cluster.name", "clarke").build();
//        Client client = new TransportClient(settings)
//        .addTransportAddress(new InetSocketTransportAddress(
//                "10.0.0.12", 9300));
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
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                if (indexContainsDoc(client, hit)) {
                    updateDoc(client, hit);
                } else {
                    indexDoc(client, hit);
                }
            }

            System.out.println("Documents processed: " + counter);
            Thread.sleep(10000);

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(60000)).execute().actionGet();

            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

        System.out.println("Finished indexing");
        client.close();
    }

    protected static void indexDoc(Client client, SearchHit hit) {
        client.prepareIndex(index2Name, "document", hit.getId())
                .setSource(hit.getSource()).execute();
        counter++;
    }

    private static void updateDoc(Client client, SearchHit hit) {
        HashSet<String> inLinks = new HashSet<String>();
        inLinks.addAll((ArrayList<String>) hit.getSource().get("in-links"));

        GetResponse response = client
                .prepareGet(index2Name, "document", hit.getId()).execute()
                .actionGet();

        inLinks.addAll((ArrayList<String>) response.getSource().get("in-links"));

        UpdateRequest update = new UpdateRequest();
        update.index(index2Name);
        update.type("document");
        update.id(hit.getId());
        try {
            update.doc(XContentFactory.jsonBuilder().startObject()
                    .field("in-links", inLinks.toArray()).endObject());
            client.update(update).get();
        } catch (IOException e) {
            System.out.println("Failed to create update for: " + hit.getId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        counter++;
    }

    private static boolean indexContainsDoc(Client client, SearchHit hit) {
        GetResponse response = client
                .prepareGet(index2Name, "document", hit.getId()).execute()
                .actionGet();

        return response.isExists();
    }
}
