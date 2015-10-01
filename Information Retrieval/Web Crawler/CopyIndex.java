import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class CopyIndex {

    public static void main(String args[]) {

        String index1Name = "crawler_moses";
        String index2Name = "crawler_merged";

//        Settings settings = ImmutableSettings.settingsBuilder()
//                .put("cluster.name", "clarke").build();
//        Client client = new TransportClient(settings)
//                .addTransportAddress(new InetSocketTransportAddress(
//                        "10.0.0.12", 9300));
        Node node = nodeBuilder().client(true).clusterName("clarke").node();
		Client client = node.client();

        SearchResponse scrollResp = client.prepareSearch(index1Name)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.matchAllQuery()).setSize(100).execute()
                .actionGet();

        System.out.println("Number of hits: "
                + scrollResp.getHits().getTotalHits());

        int counter = 0;
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                client.prepareIndex(index2Name, "document", hit.getId())
                .setSource(hit.getSource()).execute();
                counter++;
            }

            System.out.println("Documents indexed: " + counter);

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000)).execute().actionGet();

            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

        System.out.println("Finished indexing)");
        client.close();
    }
}
