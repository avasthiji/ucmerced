package edu.ucmerced.chealth.web;

import edu.ucmerced.chealth.datasource.health.domain.Totals;
import edu.ucmerced.chealth.search.SearchCriteria;
import edu.ucmerced.chealth.search.TotalsDTO;
import edu.ucmerced.chealth.service.SearchService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class ExportController {
    @Autowired
    private SearchService searchService;

    @GetMapping("/export")
    public ResponseEntity<Resource> export(
            @RequestParam(value = "region", required = false) List<Long> regions,
            @RequestParam("county") List<Long> counties,
            @RequestParam("disease") List<Long> diseases,
            @RequestParam("ethnicity") List<Long> ethnicity,
            @RequestParam("ageGroup") List<Long> ageGroups,
            @RequestParam("sex") List<String> sexes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CSVPrinter printer = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);
        printer.printRecord("Disease", "Region", "County", "Ethnicity", "SEX", "Age_Group",
                "Case", "Cost");
        for (Totals totals: searchService.search(new SearchCriteria(regions, counties, diseases,
                        ethnicity, ageGroups, sexes))) {
            TotalsDTO dto = new TotalsDTO(totals);
            printer.printRecord(dto.getDiseaseName(), dto.getRegionName(), dto.getCountyName(),
                    dto.getEthnicity(), dto.getSex(), dto.getGroupName(),
                    String.format("%.2f", dto.getCases()),
                    String.format("%.2f", dto.getCosts()));
        }
        printer.flush();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CBCD.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
    }
}
