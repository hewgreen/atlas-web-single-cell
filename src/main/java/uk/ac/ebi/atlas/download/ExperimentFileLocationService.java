package uk.ac.ebi.atlas.download;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.resource.DataFileHub;

import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Component
@NonNullByDefault
public class ExperimentFileLocationService {
    private final DataFileHub dataFileHub;

    private static final String EXPERIMENT_FILES_URI_TEMPLATE =
            "experiment/{0}/download?fileType={1}&accessKey={2}";
    private static final String EXPERIMENT_FILES_ARCHIVE_URI_TEMPLATE =
            "experiment/{0}/download/zip?fileType={1}&accessKey={2}";

    public ExperimentFileLocationService(DataFileHub dataFileHub) {
        this.dataFileHub = dataFileHub;
    }

    @Nullable
    public Path getFilePath(String experimentAccession, ExperimentFileType fileType) {
        switch (fileType) {
            case EXPERIMENT_DESIGN:
                return dataFileHub
                        .getSingleCellExperimentFiles(experimentAccession)
                        .experimentFiles.experimentDesign.getPath();
            case SDRF:
                return dataFileHub.getSingleCellExperimentFiles(experimentAccession).experimentFiles.sdrf.getPath();
            case IDF:
                return dataFileHub.getSingleCellExperimentFiles(experimentAccession).experimentFiles.idf.getPath();
            case CLUSTERING:
                return dataFileHub.getSingleCellExperimentFiles(experimentAccession).clustersTsv.getPath();
            default:
                return null;
        }
    }

    @Nullable
    public List<Path> getFilePathsForArchive(String experimentAccession, ExperimentFileType fileType) {
        switch (fileType) {
            case QUANTIFICATION_FILTERED:
                return
                        ImmutableList.of(
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).tpmsMatrix.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).cellIdsTsv.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).geneIdsTsv.getPath());
            case MARKER_GENES:
                return dataFileHub.getSingleCellExperimentFiles(experimentAccession).markerGeneTsvs
                        .values()
                        .stream()
                        .map(AtlasResource::getPath)
                        .collect(Collectors.toList());
            case QUANTIFICATION_RAW:
                return
                        ImmutableList.of(
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).filteredCountsMatrix.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).filteredCountsCellIdsTsv.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).filteredCountsGeneIdsTsv.getPath());
            case NORMALISED:
                return
                        ImmutableList.of(
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).normalisedCountsMatrix.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).normalisedCountsCellIdsTsv.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).normalisedCountsGeneIdsTsv.getPath());
            case EXPERIMENT_METADATA:
                return
                        ImmutableList.of(
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).experimentFiles.sdrf.getPath(),
                                dataFileHub.getSingleCellExperimentFiles(experimentAccession).experimentFiles.idf.getPath());

            default:
                return null;
        }
    }

    public URI getFileUri(String experimentAccession, ExperimentFileType fileType, String accessKey) {
        String uri = fileType.isArchive() ?
                MessageFormat.format(
                        EXPERIMENT_FILES_ARCHIVE_URI_TEMPLATE, experimentAccession, fileType.getId(), accessKey) :
                MessageFormat.format(
                        EXPERIMENT_FILES_URI_TEMPLATE, experimentAccession, fileType.getId(), accessKey);

        return URI.create(uri);
    }
}
