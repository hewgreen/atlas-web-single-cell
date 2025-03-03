package uk.ac.ebi.atlas.experimentimport;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ScxaExperimentDtoResultSetExtractor extends ExperimentDtoResultSetExtractor {
    @Override
    protected ExperimentDto createExperimentDto(ResultSet resultSet, String experimentAccession) throws SQLException {
        ExperimentType experimentType = ExperimentType.valueOf(resultSet.getString("type"));
        String species = resultSet.getString("species");
        Date lastUpdate = resultSet.getTimestamp("last_update");
        boolean isPrivate = resultSet.getBoolean("private");
        String accessKeyUUID = resultSet.getString("access_key");
        String title = StringUtils.isEmpty(resultSet.getString("title")) ? "" : resultSet.getString("title");

        String pubMedIdsString = resultSet.getString("pubmed_ids");

        Set<String> pubMedIds = resultSet.wasNull() || StringUtils.isBlank(pubMedIdsString)?
                new HashSet<>() :
                Sets.newHashSet(Splitter.on(", ").split(pubMedIdsString));

        String doisString = resultSet.getString("dois");

        Set<String> dois = resultSet.wasNull() || StringUtils.isBlank(doisString) ?
                new HashSet<>() :
                Sets.newHashSet(Splitter.on(", ").split(doisString));

        return new ExperimentDto(
                experimentAccession,
                experimentType,
                species,
                pubMedIds,
                dois,
                lastUpdate,
                isPrivate,
                accessKeyUUID);
    }
}
