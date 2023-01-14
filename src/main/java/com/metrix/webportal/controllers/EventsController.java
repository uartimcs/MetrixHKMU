package com.metrix.webportal.controllers;

import java.util.Optional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import com.metrix.webportal.models.Events;
import com.metrix.webportal.models.Venues;
import com.metrix.webportal.repos.EventsRepo;
import com.metrix.webportal.repos.VenuesRepo;
import com.metrix.webportal.validation.MetrixException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//1. Annotation for Web MVC Controller
@Controller
//2. Annotation for handling request from "/events","/",""
@RequestMapping(value = {"/events","/",""})
public class EventsController {
    private static final String VIEW_PREFIX = "events/";

    //3. Annotation for Dependency injection
    @Autowired
    private EventsRepo repo;

    //3. Annotation for Dependency injection
    @Autowired
    private VenuesRepo venuesRepo;

    //4a. Annotation for handling HTTP GET request from "","/","/index"
    @GetMapping({"","/","/index"})
    public String index(ModelMap m){
        /*5. get list of events from DB*/
        m.addAttribute("allEvents", repo.findAll());
        return VIEW_PREFIX + "index";
    }

    //4b. Annotation for handling HTTP GET request from "/create"
    @GetMapping("/create")
    public String create(ModelMap m){
        /*6. create an empty Event */
        m.addAttribute("newEvent", new Events());
        /*7. get list of venue from DB*/
        m.addAttribute("allVenues", venuesRepo.findAll());
        return VIEW_PREFIX + "create";
    }

    //8a. Annotation for handling HTTP POST request from "/create"
    @PostMapping("/create")
    /*9. Annotation for hibernate validation against newEvent object*/
    /*10a. Annotation for mapping HTML from body with name "newEvent"*/
    public String create(ModelMap m, @Valid @ModelAttribute("newEvent") Events newEvent, BindingResult result){
        /*11. if binding result has error, assign suitable view objects (there are 2) and return the view name VIEW_PREFIX + "create". You can refer to line 35-37 */
        if(result.hasErrors()) {
            m.addAttribute("newEvent", newEvent);
            m.addAttribute("allVenues", venuesRepo.findAll());
            return VIEW_PREFIX + "create";
        }
        /*12. set the isStartSell to false, and then save newEvent object to DB */
        newEvent.setStartSell(false);
        repo.save(newEvent);
        return "redirect:/" + VIEW_PREFIX + "index";
    }

    //4c. Annotation for handling HTTP GET request from "/update/{id}"
    @GetMapping("/update/{id}")
    /*13. Annotation for mapping the variable "id" in the path*/
    public String update(ModelMap m, @PathVariable("id") Integer id) throws MetrixException{
        /*14. Find the Events from DB, if not found, throw the following exception
        throw new MetrixException(-1, String.format("Event with id {%d} not found!", id), "/" + VIEW_PREFIX + "index"); */
        Optional<Events> aEvent = repo.findById(id);
        if(!aEvent.isPresent()) {
            throw new MetrixException(-1, String.format("Event with id {%d} not found!", id), "/" + VIEW_PREFIX + "index");
        }
        /*15. pass the Events object from DB */
        m.addAttribute("updEvent", aEvent.get());
        /*16. get the list of venues from DB */
        m.addAttribute("allVenues", venuesRepo.findAll());
        return VIEW_PREFIX + "update";
    }

    //8b. Annotation for handling HTTP POST request from /update/{id}
    @PostMapping("/update/{id}")
    /*9. Annotation for hibernate validation against updEvent object*/
    /*10b. Annotation for mapping HTML from body with name "updEvent"*/
    /*13. Annotation for mapping the variable "id" in the path*/
    public String update(ModelMap m, @PathVariable("id") Integer id, @Valid @ModelAttribute("updEvent") Events updEvent, BindingResult result) throws MetrixException{
        /*14. Find the Events from DB, if not found, throw the following exception
        throw new MetrixException(-1, String.format("Event with id {%d} not found!", id), "/" + VIEW_PREFIX + "index"); */
        Optional<Events> aEvent = repo.findById(id);
        if(!aEvent.isPresent()) {
            throw new MetrixException(-1, String.format("Event with id {%d} not found!", id), "/" + VIEW_PREFIX + "index");
        }
        /* 15. if there is change of venue, while this event linked to any tickets, throw the following exception
        throw new MetrixException(-1, "Event already has ticket sold, change of venue is not allowed", "/" + VIEW_PREFIX + "update/" + id); */
        Events event = aEvent.get();        
        String originalName = event.getVenue().getVenueName();
        String newName = updEvent.getVenue().getVenueName();
        
        if(!originalName.equals(newName) && !event.getTickets().isEmpty()) {
            throw new MetrixException(-1, "Event already has ticket sold, change of venue is not allowed", "/" + VIEW_PREFIX + "update/" + id);
        }
        /*16. if binding result has error, assign suitable view objects (there are 2) and return the view name VIEW_PREFIX + "update". You can refer to line 52-54 */
        if(result.hasErrors()) {
            m.addAttribute("updEvent", updEvent);
            m.addAttribute("allVenues", venuesRepo.findAll());
            return VIEW_PREFIX + "update";
        }
        /*17. save the updEvent to DB */
        repo.save(updEvent);
        return "redirect:/" + VIEW_PREFIX + "index";
    }

    //4c. Annotation for handling HTTP GET request from "/delete/{id}"
    @GetMapping("/delete/{id}")
    /*13. Annotation for mapping the variable "id" in the path*/
    public String delete(@PathVariable("id") Integer id) throws MetrixException{
        /*14. Find the Events from DB, if not found, throw the following exception
        throw new MetrixException(-1, String.format("Event with id {%d} not found!", id), "/" + VIEW_PREFIX + "index"); */
        Optional<Events> aEvent = repo.findById(id);
        if(!aEvent.isPresent()) {
            throw new MetrixException(-1, String.format("Event with id {%d} not found!", id), "/" + VIEW_PREFIX + "index");
        }

        /*18. if this event already have ticket sold, throw the following exception
        throw new MetrixException(-1, "Event already has ticket sold, delete is not allowed! Please delete corresponding ticket(s) first", "/" + VIEW_PREFIX + "index"); */
        Events event = aEvent.get();
        if(!event.getTickets().isEmpty()) {
            throw new MetrixException(-1, "Event already has ticket sold, delete is not allowed! Please delete corresponding ticket(s) first", "/" + VIEW_PREFIX + "index");
        }
        /*19. delete the record from DB by id */
        repo.deleteById(id);
        return "redirect:/" + VIEW_PREFIX + "index";
    }
}