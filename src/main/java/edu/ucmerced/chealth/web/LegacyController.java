package edu.ucmerced.chealth.web;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
@CommonsLog
public class LegacyController {
    /**
     * Redirect permanently from the old /Health context-path to /.
     */
    @GetMapping("/Health/**")
    public RedirectView getLegacyHome(HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String requesturi = request.getRequestURI();
        if (requesturi.startsWith("/Health/")) {
            redirectView.setUrl(requesturi.substring(7));
        } else {
            redirectView.setUrl("/");
        }
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return redirectView;
    }
}
