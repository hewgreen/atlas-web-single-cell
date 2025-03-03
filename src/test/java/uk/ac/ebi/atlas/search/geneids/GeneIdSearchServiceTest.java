package uk.ac.ebi.atlas.search.geneids;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.solr.BioentityPropertyName;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesProperties;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.ID_PROPERTY_NAMES;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES_OVERRIDE_PROPERTY_NAMES;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomKnownBioentityPropertyName;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeneIdSearchServiceTest {
    private static final Species HUMAN =
            new Species(
                    "Homo sapiens",
                    SpeciesProperties.create("Homo_sapiens", "ORGANISM_PART", "animals", ImmutableList.of()));

    @Mock
    private GeneIdSearchDao geneIdSearchDaoMock;

    private InOrder inOrder;

    private GeneIdSearchService subject;

    @BeforeEach
    void setUp() {
        subject = new GeneIdSearchService(geneIdSearchDaoMock);
        inOrder = inOrder(geneIdSearchDaoMock);
    }

    @Test
    void geneQueryWithoutCategoryIsSearchedInIdProperties() {
        subject.search(GeneQuery.create("foobar"));
        subject.search(GeneQuery.create("foobar", HUMAN));

        ID_PROPERTY_NAMES.forEach(propertyName ->
                inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", propertyName.name));

        ID_PROPERTY_NAMES.forEach(propertyName ->
                inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName()));
    }

    @Test
    void speciesSpecificCategoriesIgnoreSpecies() {
        SPECIES_OVERRIDE_PROPERTY_NAMES.forEach(propertyName -> {
            subject.search(GeneQuery.create("foobar", propertyName, HUMAN));
            inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", propertyName.name);
        });

        SPECIES_OVERRIDE_PROPERTY_NAMES.forEach(propertyName ->
            verify(geneIdSearchDaoMock, never()).searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName()));
    }

    @Test
    void multiSpeciesCategoriesHonourSpecies() {
        BioentityPropertyName propertyName = generateRandomKnownBioentityPropertyName();
        while (SPECIES_OVERRIDE_PROPERTY_NAMES.contains(propertyName)) {
            propertyName = generateRandomKnownBioentityPropertyName();
        }

        subject.search(GeneQuery.create("foobar", propertyName, HUMAN));
        verify(geneIdSearchDaoMock).searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName());
    }

    @Test
    void ifNoIdMatchesWeGetEmptyOptional() {
        assertThat(subject.search(GeneQuery.create("foobar", HUMAN)))
                .isEqualTo(subject.search(GeneQuery.create("foobar")))
                .isEmpty();
    }

    @Test
    void ifAtLeastOneIdMatchesWeGetNonEmptyOptional() {
        BioentityPropertyName randomIdPropertyName = generateRandomKnownBioentityPropertyName();
        while (!SPECIES_OVERRIDE_PROPERTY_NAMES.contains(randomIdPropertyName)) {
            randomIdPropertyName = generateRandomKnownBioentityPropertyName();
        }

        ID_PROPERTY_NAMES.forEach(propertyName -> {
            when(geneIdSearchDaoMock.searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName()))
                    .thenReturn(Optional.empty());
            when(geneIdSearchDaoMock.searchGeneIds("foobar", propertyName.name))
                    .thenReturn(Optional.empty());
        });

        when(geneIdSearchDaoMock.searchGeneIds("foobar", randomIdPropertyName.name, HUMAN.getEnsemblName()))
                .thenReturn(Optional.of(ImmutableSet.of()));
        when(geneIdSearchDaoMock.searchGeneIds("foobar", randomIdPropertyName.name))
                .thenReturn(Optional.of(ImmutableSet.of()));

        assertThat(subject.search(GeneQuery.create("foobar", HUMAN)))
                .hasValue(ImmutableSet.of());
        assertThat(subject.search(GeneQuery.create("foobar")))
                .hasValue(ImmutableSet.of());
    }

    @Test
    void resultsOfFirstIdThatMatchesAreReturned() {
        BioentityPropertyName randomIdPropertyName = generateRandomKnownBioentityPropertyName();
        while (!ID_PROPERTY_NAMES.contains(randomIdPropertyName)) {
            randomIdPropertyName = generateRandomKnownBioentityPropertyName();
        }

        ImmutableList<BioentityPropertyName> idPropertyNamesBefore =
                ID_PROPERTY_NAMES.subList(0, ID_PROPERTY_NAMES.indexOf(randomIdPropertyName));

        ImmutableList<BioentityPropertyName> idPropertyNamesAfter =
                ID_PROPERTY_NAMES.subList(
                        ID_PROPERTY_NAMES.indexOf(randomIdPropertyName) + 1, ID_PROPERTY_NAMES.size());

        idPropertyNamesBefore.forEach(propertyName -> {
            when(geneIdSearchDaoMock.searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName()))
                    .thenReturn(Optional.of(ImmutableSet.of()));
            when(geneIdSearchDaoMock.searchGeneIds("foobar", propertyName.name))
                    .thenReturn(Optional.of(ImmutableSet.of()));
        });

        idPropertyNamesAfter.forEach(propertyName -> {
            when(geneIdSearchDaoMock.searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName()))
                    .thenReturn(Optional.of(ImmutableSet.of("ENSFOOBAR0000002")));
            when(geneIdSearchDaoMock.searchGeneIds("foobar", propertyName.name))
                    .thenReturn(Optional.of(ImmutableSet.of("ENSFOOBAR0000002")));
        });

        when(geneIdSearchDaoMock.searchGeneIds("foobar", randomIdPropertyName.name, HUMAN.getEnsemblName()))
                .thenReturn(Optional.of(ImmutableSet.of("ENSFOOBAR0000001")));
        when(geneIdSearchDaoMock.searchGeneIds("foobar", randomIdPropertyName.name))
                .thenReturn(Optional.of(ImmutableSet.of("ENSFOOBAR0000001")));

        assertThat(subject.search(GeneQuery.create("foobar", HUMAN)))
                .isEqualTo(subject.search(GeneQuery.create("foobar")))
                .hasValue(ImmutableSet.of("ENSFOOBAR0000001"));

        idPropertyNamesBefore.forEach(propertyName ->
                inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName()));
        inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", randomIdPropertyName.name, HUMAN.getEnsemblName());

        idPropertyNamesBefore.forEach(propertyName ->
                inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", propertyName.name));
        inOrder.verify(geneIdSearchDaoMock).searchGeneIds("foobar", randomIdPropertyName.name);

        idPropertyNamesAfter.forEach(propertyName -> {
            verify(geneIdSearchDaoMock, never()).searchGeneIds("foobar", propertyName.name, HUMAN.getEnsemblName());
            verify(geneIdSearchDaoMock, never()).searchGeneIds("foobar", propertyName.name);
        });
    }

    @Test
    void ifQueryHasEmptySpeciesSearchAllSpecies() {
        var searchString = randomAlphanumeric(3, 20);
        subject.search(GeneQuery.create(searchString));
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "ensgene");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "symbol");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "entrezgene");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "hgnc_symbol");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "mgi_id");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "mgi_symbol");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "flybase_gene_id");
        verify(geneIdSearchDaoMock).searchGeneIds(searchString, "wbpsgene");
    }
}
