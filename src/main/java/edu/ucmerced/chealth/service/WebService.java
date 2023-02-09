package edu.ucmerced.chealth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;

@Service
public class WebService {
    @Value("${feedbackEmail:}")
    private String feedbackEmail;

    @Autowired
    private BuildProperties buildProperties;

    public void addGeneralParameters(Model model) {
        model.addAttribute("appVersion", getAppVersion());
        model.addAttribute("copyrightYear", getCopyrightYear());
        model.addAttribute("feedbackEmail", feedbackEmail);
    }

    private String getAppVersion() {
        return buildProperties.getVersion();
    }

    private String getCopyrightYear() {
        return String.valueOf(LocalDate.now().getYear());
    }
}
