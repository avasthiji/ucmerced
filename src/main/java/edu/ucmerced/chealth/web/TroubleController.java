package edu.ucmerced.chealth.web;

import edu.ucmerced.chealth.service.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class TroubleController implements ErrorController {
    @Autowired
    private WebService webService;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        webService.addGeneralParameters(model);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
        }
        return "error/general";
    }
}
