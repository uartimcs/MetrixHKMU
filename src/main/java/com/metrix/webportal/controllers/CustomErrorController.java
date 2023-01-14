package com.metrix.webportal.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import com.metrix.webportal.validation.MetrixException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//1. Annotation for Web MVC Controller
@Controller
public class CustomErrorController implements ErrorController {
    
    //2. Annotation for handling HTTP GET request from "/customError"
    @GetMapping("/customError")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        MetrixException err = new MetrixException();

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
        
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                err = new MetrixException(404, "Page not found!", "/");
            } else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                err = new MetrixException(500, "Server error!", "/");
            }
        } 
        
        if(err.getCustomErrMsg() == null || err.getCustomErrMsg().isEmpty()) {
            err = new MetrixException(-1, "Unknown Error Occur!", "/");
        }
        //3. Return with view name "error", with view Object name "ErrObject" and assign err as view object
        return new ModelAndView("error", "ErrObject", err);
    } 
}
