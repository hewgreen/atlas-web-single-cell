package uk.ac.ebi.atlas.search.metadata;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.atlas.controllers.JsonExceptionHandlingController;

import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@RestController
public class JsonMetadataSearchController extends JsonExceptionHandlingController {

    private final MetadataSearchDao metadataSearchDao;

    public JsonMetadataSearchController(MetadataSearchDao metadataSearchDao) {
        this.metadataSearchDao = metadataSearchDao;
    }

    @RequestMapping(value = "/json/metadata-search",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String search(@RequestParam MultiValueMap<String, String> requestParams) {
        String searchTerm = requestParams.getFirst("q");

        MetadataQuery query = MetadataQuery.create(searchTerm);

        return GSON.toJson(metadataSearchDao.searchCellIdsByMetadata(query.queryTerm()));
    }
}
