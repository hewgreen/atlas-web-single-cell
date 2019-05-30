package uk.ac.ebi.atlas.search.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetadaSearchDaoIT {

    @Inject
    private SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory;

    private MetadataSearchDao subject;

    @BeforeEach
    void setUp() {
        subject = new MetadataSearchDao(solrCloudCollectionProxyFactory);
    }

    @Test
    void searchForValidMetadataReturnsCellIds() {
        assertThat(subject.search("organism part")).isNotEmpty();
    }

    @Test
    void searchForInvalidMetadataReturnsEmpty() {
        assertThat(subject.search("foobar")).isEmpty();
    }

}
