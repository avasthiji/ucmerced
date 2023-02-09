package edu.ucmerced.chealth.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.ucmerced.chealth.search.SearchCriteria;
import edu.ucmerced.chealth.search.SearchUtils;
import edu.ucmerced.chealth.service.SearchFormatter;
import edu.ucmerced.chealth.service.SearchService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CommonsLog
public class SearchController {
    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchFormatter searchFormatter;

    @GetMapping("/search")
    public ObjectNode search(
            @RequestParam(value = "region", required = false) List<Long> regions,
            @RequestParam("county") List<Long> counties,
            @RequestParam("disease") List<Long> diseases,
            @RequestParam("ethnicity") List<Long> ethnicity,
            @RequestParam("ageGroup") List<Long> ageGroups,
            @RequestParam("sex") List<String> sexes
    ) {
        return searchFormatter.createSearchResult(searchService.search(
                new SearchCriteria(regions, counties, diseases, ethnicity, ageGroups, sexes)),
                SearchUtils.notNullList(regions));
    }
}
