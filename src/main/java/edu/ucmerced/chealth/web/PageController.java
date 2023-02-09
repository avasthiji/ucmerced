package edu.ucmerced.chealth.web;

import edu.ucmerced.chealth.service.WebService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CommonsLog
public class PageController {
    @Autowired
    private WebService webService;

    @GetMapping("/")
    public String getIndex(Model model) {
        return handlePage("index", model);
    }

    @GetMapping("/start")
    public String getStartPage(Model model) {
        return handlePage("start", model);
    }

    @GetMapping("/faq")
    public String getFaqPage(Model model) {
        return handlePage("faq", model);
    }

    @GetMapping("/app")
    public String getAppPage(Model model) {
        return handlePage("app", model);
    }

    @GetMapping("/about")
    public String getAboutPage(Model model) {
        return handlePage("about", model);
    }

    private String handlePage(String page, Model model) {
        webService.addGeneralParameters(model);
        model.addAttribute("currentPage", page);
        return page;
    }
}
