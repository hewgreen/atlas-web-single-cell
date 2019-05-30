package uk.ac.ebi.atlas.search.metadata;

import com.google.auto.value.AutoValue;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

// TODO Not sure if an explicit metadata query class is needed, it's more likely we just need to refactor GeneQuery.
@AutoValue
public abstract class MetadataQuery {
    public abstract String queryTerm();

    // TODO create query term category class (similar to BioentityPropertyName, but for metadata/factor/characteristic types)
    public abstract Optional<String> category();

    public static MetadataQuery create(String queryTerm) {
        return create(queryTerm, Optional.empty());
    }

    public static MetadataQuery create(String queryTerm, String category) {
        return create(queryTerm, Optional.of(category));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static MetadataQuery create(String queryTerm,
                                    Optional<String> category) {
        if (isBlank(queryTerm)) {
            throw new IllegalArgumentException("Query term cannot be blank");
        }
        return new AutoValue_MetadataQuery(queryTerm, category);
    }
}
