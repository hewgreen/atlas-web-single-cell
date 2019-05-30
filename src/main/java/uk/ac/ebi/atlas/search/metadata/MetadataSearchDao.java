package uk.ac.ebi.atlas.search.metadata;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.SingleCellAnalyticsSchemaField;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.CELL_ID;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.ONTOLOGY_ANCESTOR_LABELS;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.ONTOLOGY_ANNOTATION_LABEL;
import static uk.ac.ebi.atlas.solr.cloud.collections.SingleCellAnalyticsCollectionProxy.ONTOLOGY_PART_OF_LABELS;

@Component
public class MetadataSearchDao {

    private SingleCellAnalyticsCollectionProxy singleCellAnalyticsCollectionProxy;

    public MetadataSearchDao(SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        this.singleCellAnalyticsCollectionProxy =
                solrCloudCollectionProxyFactory.create(SingleCellAnalyticsCollectionProxy.class);
    }

    // Returns cell IDs grouped by experiment
    public Map<String, ImmutableSet<String>> searchCellIdsByMetadata(String metadataValue) {
        var searchTermAsCollection = Collections.singletonList(metadataValue);

        ImmutableMap<SingleCellAnalyticsSchemaField, Collection<String>> queryFields = ImmutableMap.of(
                ONTOLOGY_ANNOTATION_LABEL, searchTermAsCollection,
                ONTOLOGY_ANCESTOR_LABELS, searchTermAsCollection,
                ONTOLOGY_PART_OF_LABELS, searchTermAsCollection);

        var queryBuilder =
                new SolrQueryBuilder<SingleCellAnalyticsCollectionProxy>()
                        .addQueryFieldByTerm(queryFields)
                        .setFieldList(Arrays.asList(EXPERIMENT_ACCESSION, CELL_ID));

        return singleCellAnalyticsCollectionProxy.query(queryBuilder).getResults()
                .stream()
                .collect(groupingBy(x -> x.get("experiment_accession").toString(),
                        mapping(x -> x.get("cell_id").toString(), toImmutableSet())));
    }

}
