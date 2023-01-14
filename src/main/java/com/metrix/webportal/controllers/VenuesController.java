package com.metrix.webportal.controllers;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.metrix.webportal.models.Venues;
import com.metrix.webportal.repos.VenuesRepo;
import com.metrix.webportal.validation.MetrixException;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/venues")
public class VenuesController {
    
    private static final String VIEW_PREFIX = "venues/";

    @Autowired
    private VenuesRepo repo;

    @GetMapping({"","/","/index"})
    public String index(ModelMap m){
        m.addAttribute("allVenues", repo.findAll());
        return VIEW_PREFIX + "index";
    }

    @GetMapping("/create")
    public String create(ModelMap m){
        m.addAttribute("newVenue", new Venues());
        return VIEW_PREFIX + "create";
    }

    @PostMapping("/create")
    public String create(ModelMap m, @Valid @ModelAttribute("newVenue") Venues newVenues, BindingResult result){
        if(result.hasErrors()){
            m.addAttribute("newVenue", newVenues);
            return VIEW_PREFIX + "create";
        }
        repo.save(newVenues);
        return "redirect:/" + VIEW_PREFIX + "index";
    }

    @GetMapping("/update/{id}")
    public String update(ModelMap m, @PathVariable("id")Integer id) throws MetrixException{
        Optional<Venues> oVenue = repo.findById(id);
        //if venue with id not found, throw exception, include redirect url to /venues/index in error page
        if(!oVenue.isPresent()){
            throw new MetrixException(-1, String.format("Venue with id {%d} not found!", id), "/" + VIEW_PREFIX + "index");
        }
        m.addAttribute("updVenue", oVenue.get());
        return VIEW_PREFIX + "update";
    }

    @PostMapping("/update/{id}")
    public String update(ModelMap m, @PathVariable("id")Integer id, @Valid @ModelAttribute("updVenue") Venues updVenues, BindingResult result) throws MetrixException{
        Optional<Venues> oVenue = repo.findById(id);
        //if venue with id not found, throw exception, include redirect url to /venues/index in error page
        if(!oVenue.isPresent()){
            throw new MetrixException(-1, String.format("Venue with id {%d} not found!", id), "/" + VIEW_PREFIX + "index");
        }

        Venues venues = oVenue.get();
        //if there is reduction of number of seat, while this venue linked to any events, throw exception, include redirect url to /venues/update/{id} in error page
        if(venues.getNumberOfSeat() > updVenues.getNumberOfSeat() && !venues.getEvents().isEmpty()){
            throw new MetrixException(-1, "Venue already linked with event(s), reduce seat number is not allowed", "/" + VIEW_PREFIX + "update/" + id);
        }

        if(result.hasErrors()){
            m.addAttribute("updVenue", updVenues);
            return VIEW_PREFIX + "update";
        }
        repo.save(updVenues);
        return "redirect:/" + VIEW_PREFIX + "index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id")Integer id) throws MetrixException{
        Optional<Venues> oVenue = repo.findById(id);
        //if venue with id not found, throw exception, include redirect url to /venues/index in error page
        if(!oVenue.isPresent()){
            throw new MetrixException(-1, String.format("Venue with id {%d} not found!", id), "/" + VIEW_PREFIX + "index");
        }

        Venues venues = oVenue.get();
        //if this venue linked to any events, throw exception, include redirect url to /venues/index in error page
        if(!venues.getEvents().isEmpty()){
            throw new MetrixException(-1, "Venue already linked with event(s), delete is not allowed! Please delete corresponding event(s) first", "/" + VIEW_PREFIX + "index");
        }
        repo.deleteById(id);
        return "redirect:/" + VIEW_PREFIX + "index";
    }  
}
