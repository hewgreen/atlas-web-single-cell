package uk.ac.ebi.atlas.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.experimentpage.ExperimentAttributesService;
import uk.ac.ebi.atlas.search.geneids.GeneIdSearchService;
import uk.ac.ebi.atlas.search.geneids.GeneQuery;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ScxaExperimentTrader;

import javax.inject.Inject;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@WebAppConfiguration
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = TestConfig.class)
class JsonGeneSearchControllerTest {
    @Mock
    private GeneIdSearchService geneIdSearchServiceMock;

    @Inject
    private SpeciesFactory speciesFactory;

    @Inject
    private GeneSearchService geneSearchService;

    @Inject
    private ScxaExperimentTrader experimentTrader;

    @Inject
    private ExperimentAttributesService experimentAttributesService;

    private JsonGeneSearchController subject;

    @BeforeEach
    void setUp() {
        subject =
                new JsonGeneSearchController(
                        geneIdSearchServiceMock,
                        speciesFactory,
                        geneSearchService,
                        experimentTrader,
                        experimentAttributesService);
    }

    @Test
    void ifSpeciesIsNotPresentGeneQueryHasEmptySpeciesField() {
        var requestParams = new LinkedMultiValueMap<String, String>();
        requestParams.add("q", randomAlphabetic(1, 12));
        subject.search(requestParams);

        var geneQueryArgCaptor = ArgumentCaptor.forClass(GeneQuery.class);
        verify(geneIdSearchServiceMock).search(geneQueryArgCaptor.capture());

        assertThat(geneQueryArgCaptor.getValue().species()).isEmpty();
    }
}