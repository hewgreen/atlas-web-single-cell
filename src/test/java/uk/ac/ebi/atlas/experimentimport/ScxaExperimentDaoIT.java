package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ScxaExperimentDaoIT {
    private static final String SC_ACCESSION = "TEST-SC";
    private static final UUID RANDOM_UUID = UUID.randomUUID();

    @Inject
    private ScxaExperimentDao subject;

    @Before
    public void setUp() throws Exception {
        ExperimentDto experimentDto =
                new ExperimentDto(
                        SC_ACCESSION,
                        ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE,
                        "Homo sapiens",
                        Sets.newHashSet("PubMed ID 1", "PubMed ID 2"),
                        Sets.newHashSet("100.100/doi", "200.200/doi"),
                        new SimpleDateFormat("yyyy-MM-dd").parse("2017-01-31"),
                        true,
                        RANDOM_UUID.toString());
        subject.addExperiment(experimentDto, RANDOM_UUID);
    }

    @After
    public void tearDown() {
        try {
            subject.deleteExperiment(SC_ACCESSION);
        } catch (ResourceNotFoundException e) {
            // Some tests remove the test experiment
        }
    }

    @Test
    public void publicExperimentsDontIncludePrivateExperiments() {
        assumeThat(
                subject.findExperiment(SC_ACCESSION, RANDOM_UUID.toString()),
                hasProperty("experimentAccession", is(SC_ACCESSION)));

        assertThat(
                subject.findPublicExperimentAccessions(),
                not(hasItem("experimentAccession")));
    }

    @Test
    public void asAdminIncludesPrivateExperiments() {
        assertThat(subject.getAllExperimentsAsAdmin(), hasItem(hasProperty("experimentAccession", is(SC_ACCESSION))));
    }

    @Test
    public void setPrivacyStatus() {
        assumeThat(subject.findPublicExperimentAccessions(), not(hasItem(SC_ACCESSION)));
        subject.setExperimentPrivacyStatus(SC_ACCESSION, false);
        assertThat(subject.findPublicExperimentAccessions(), hasItem(SC_ACCESSION));
    }

    @Test
    public void countExperiments() {
        int count = subject.countExperiments();
        subject.deleteExperiment(SC_ACCESSION);
        assertThat(subject.countExperiments(), is(count - 1));
    }

    @Test
    public void forPublicExperimentsAccessKeyIsIgnored() {
        subject.setExperimentPrivacyStatus(SC_ACCESSION, false);
        assertThat(subject.findExperiment(SC_ACCESSION, "foo"), is(subject.findExperiment(SC_ACCESSION, "bar")));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    @Test
    public void forPrivateExperimentsAccessKeyIsRequired() {
        assertThat(
                subject.findExperiment(SC_ACCESSION, RANDOM_UUID.toString()),
                hasProperty("experimentAccession", is(SC_ACCESSION)));

        exception.expect(ResourceNotFoundException.class);
        subject.findExperiment(SC_ACCESSION, "foobar");
    }
}
