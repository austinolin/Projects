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
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class Search {

    private static final String index = "merged_crawler";

    public static SearchHit[] query(String query) {
//        Settings settings = ImmutableSettings.settingsBuilder()
//                .put("cluster.name", "clarke").build();
//        Client client = new TransportClient(settings)
//                .addTransportAddress(new InetSocketTransportAddress(
//                        "10.0.0.12", 9300));
    	Node node = nodeBuilder().client(true).clusterName("clarke").node();
		Client client = node.client();

        SearchResponse scrollResp = client.prepareSearch(index).addField("url")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.matchQuery("text", query)).setSize(200)
                .execute().actionGet();

        return scrollResp.getHits().getHits();

    }
}
