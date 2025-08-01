package com.guilhermezuriel.reduceme.application.controller.pages;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String error = "Unexpected Error";

        if (statusCode != null) {
            status = Integer.parseInt(statusCode.toString());
            try {
                error = HttpStatus.valueOf(status).getReasonPhrase();
            } catch (IllegalArgumentException e) {
                error = "Unknown Error";
            }
        }

        model.addAttribute("status", status);
        model.addAttribute("error", error);
        model.addAttribute("message", errorMessage != null ? errorMessage :
                (exception != null ? exception.toString() : "An unexpected error occurred."));

        return "error";
    }
}
