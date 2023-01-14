package com.metrix.webportal.validation;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

//1. Annotation to indicate this class is controller advise to handle custom exception
@ControllerAdvice
public class ControllerAdvise {
    
    //2. Annotation to indicate this method will handle exception with type MetrixException.class
    @ExceptionHandler(MetrixException.class)
    public ModelAndView handleMetrixException(MetrixException e){
        return new ModelAndView("error", "ErrObject", e);
    }
}