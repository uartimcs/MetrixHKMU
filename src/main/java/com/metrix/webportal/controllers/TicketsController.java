package com.metrix.webportal.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import com.metrix.webportal.domains.SellForm;
import com.metrix.webportal.models.Events;
import com.metrix.webportal.models.Tickets;
import com.metrix.webportal.repos.EventsRepo;
import com.metrix.webportal.repos.TicketsRepo;
import com.metrix.webportal.validation.MetrixException;
import com.metrix.webportal.validation.TicketValidator;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//1. Annotation for Web MVC Controller
@Controller
//2. Annotation for handling request from "/tickets"
@RequestMapping("/tickets")
public class TicketsController {
    private static final String VIEW_PREFIX = "tickets/";

    private static final String HARD_CODE_OPERATOR = "Dummy Operator";

    //3. Annotation for Dependency injection
    @Autowired
    private TicketsRepo repo;

    //3. Annotation for Dependency injection
    @Autowired
    private EventsRepo eventsRepo;

    //3. Annotation for Dependency injection
    @Autowired
    private TicketValidator validator;

    //4a. Annotation for handling HTTP GET request from "/list/{eventId}"
    @GetMapping("/list/{eventId}")
    /*5. Annotation for mapping the variable "eventId" in the path*/
    public String list(ModelMap m, @PathVariable("eventId") Integer eventId) throws MetrixException{
        /*6. Find the Events from DB, if not found, throw the following exception
        throw new MetrixException(-1, "Event not found!", "/"); */
        Optional<Events> aEvent = eventsRepo.findById(eventId);
        if(!aEvent.isPresent()) {
            throw new MetrixException(-1, "Event not found!", "/");
        }
        /*7a. Get a list of tickets from events object from DB*/
        m.addAttribute("allTickets", aEvent.get().getTickets());
        m.addAttribute("ticketDetail", new Tickets());
        return VIEW_PREFIX + "list"; 
    }

    //4b. Annotation for handling HTTP GET request from "/create"
    @GetMapping("/create")
    public String create(ModelMap m){
        m.addAttribute("newSellForm", new SellForm());
        /*7b. Get a list of events object from DB*/
        m.addAttribute("allEvents", eventsRepo.findAll());
        return VIEW_PREFIX + "create";
    }

    //8a. Annotation for handling HTTP POST request from "/create"
    @PostMapping("/create")
    //9. Annotation for Spring managing DB transaction through out the whole method
    @Transactional
    /*9. Annotation for hibernate validation against newSellForm object*/
    /*10a. Annotation for mapping HTML from body with name "newSellForm"*/
    public String create(ModelMap m, @Valid @ModelAttribute("newSellForm") SellForm newSellForm, BindingResult result){
        //11. valid the newSellForm from validator and put the validation result in "result" object
        validator.validate(newSellForm, result);
        /*12. if binding result has error, assign suitable view objects (there are 2) and return the view name VIEW_PREFIX + "create". You can refer to line 54-56 */
        if(result.hasErrors()) {
            m.addAttribute("newSellForm", newSellForm);
            m.addAttribute("allEvents", eventsRepo.findAll());
            return VIEW_PREFIX + "create";
        }

        List<Tickets> ticketSold = new ArrayList<>();
        for(int i=0; i<newSellForm.getNumberOfTicket();i++){
            Tickets newTicket = new Tickets();
            newTicket.setClaimed(false);
            newTicket.setOperator(HARD_CODE_OPERATOR);
            newTicket.setSellDtm(new Date());
            newTicket.setEvent(eventsRepo.findById(newSellForm.getEventId()).get());
            //generate qr code but do while loop, you can assume it will never endless. Safe guard by only have max 100 seat
            boolean isDuplicated = true;
            do{
                String qrCode = UUID.randomUUID().toString();
                if(!repo.getTicketByQrCode(qrCode).isPresent()){
                    isDuplicated = false;
                }
                newTicket.setQrcode(qrCode);
            }while(isDuplicated);
            //13. Save the newTicket object to DB
            repo.save(newTicket);
            ticketSold.add(newTicket);
        }

        m.addAttribute("ticketSold", ticketSold);
        return VIEW_PREFIX + "ticketSold";
    }

    //4c. Annotation for handling HTTP GET request from "","/","/query"
    @GetMapping({"","/","/query"})
    public String query(ModelMap m){
        m.addAttribute("ticketDetail", new Tickets());
        return VIEW_PREFIX + "query";
    }

    //8b. Annotation for handling HTTP POST request from "/detail"
    @PostMapping("/detail")
    /*10b. Annotation for mapping HTML from body with name "ticketDetail"*/
    public String detail(ModelMap m, @ModelAttribute("ticketDetail") Tickets queryTicket) throws MetrixException{
        if(queryTicket.getQrcode().trim().isEmpty()){
            throw new MetrixException(-1, "Ticket QR code must be provided!", "/" + VIEW_PREFIX + "query");
        }
        /*14. Find the Tickets from DB by QR Code. You should create a custom function in TicketRepo interface, with custom JPQL to filter against the qrcode field
        If not found, throw the following exception
        throw new MetrixException(-2, "Ticket not found!", "/" + VIEW_PREFIX + "query");*/
        
        String queryTicketCode = queryTicket.getQrcode().trim();
        Optional<Tickets> aTicket = repo.getTicketByQrCode(queryTicketCode);
        if(!aTicket.isPresent()) {
            throw new MetrixException(-2, "Ticket not found!", "/" + VIEW_PREFIX + "query");
        }
        /*15. Get the Tickets object from the return of Q.14 */
        m.addAttribute("ticketDetail", aTicket.get());
        return VIEW_PREFIX + "detail"; 
    }

    //8c. Annotation for handling HTTP POST request from "/claim"
    @PostMapping("/claim")
    /*10b. Annotation for mapping HTML from body with name "ticketDetail"*/
    public String claim(ModelMap m, @ModelAttribute("ticketDetail") Tickets claimTicket) throws MetrixException{
        if(claimTicket.getQrcode().trim().isEmpty()){
            throw new MetrixException(-1, "Ticket QR code must be provided!", "/" + VIEW_PREFIX + "query");
        }
        /*14. Find the Tickets from DB by QR Code. You should create a custom function in TicketRepo interface, with custom JPQL to filter against the qrcode field
        If not found, throw the following exception
        throw new MetrixException(-2, String.format("Ticket with QR Code {%s} not found!", claimTicket.getQrcode()), "/" + VIEW_PREFIX + "query");*/        
        String claimTicketCode = claimTicket.getQrcode().trim();
        Optional<Tickets> aTicket = repo.getTicketByQrCode(claimTicketCode);
        if(!aTicket.isPresent()) {
            throw new MetrixException(-2, String.format("Ticket with QR Code {%s} not found!", claimTicket.getQrcode()), "/" + VIEW_PREFIX + "query");
        }
        /*15. If the isClaimed is true for the Tickets object returned from Q.14, throw the following exception
        throw new MetrixException(-2, String.format("Ticket with QR Code {%s} already claimed!", claimTicket.getQrcode()), "/" + VIEW_PREFIX + "query");*/
        Tickets ticket = aTicket.get();
        if(ticket.isClaimed()) {
            throw new MetrixException(-2, String.format("Ticket with QR Code {%s} already claimed!", claimTicket.getQrcode()), "/" + VIEW_PREFIX + "query");
        }
        //Set the isClaimed to true and then save to DB
        ticket.setClaimed(true);
        ticket.setClaimDtm(new Date());
        //16. Save the ticket object to DB
        repo.save(ticket);
        m.addAttribute("ticketDetail", ticket);
        return VIEW_PREFIX + "detail";     
    }

    //4d. Annotation for handling HTTP GET request from /delete/{qrcode}
    @GetMapping("/delete/{qrcode}")
    /*5. Annotation for mapping the variable "qrcode" in the path*/
    public String delete(@PathVariable("qrcode") String qrcode) throws MetrixException{
        /*14. Find the Tickets from DB by QR Code. You should create a custom function in TicketRepo interface, with custom JPQL to filter against the qrcode field
        If not found, throw the following exception
        throw new MetrixException(-2, String.format("Ticket with QR Code {%s} not found!", qrcode), "/" + VIEW_PREFIX + "query");*/
        Optional<Tickets> aTicket = repo.getTicketByQrCode(qrcode);
        if(!aTicket.isPresent()) {
            throw new MetrixException(-2, String.format("Ticket with QR Code {%s} not found!", qrcode), "/" + VIEW_PREFIX + "query");
        }
        /*15. If the isClaimed is true for the Tickets object returned from Q.14, throw the following exception
        throw new MetrixException(-2, String.format("Ticket with QR Code {%s} already claimed!", qrcode), "/" + VIEW_PREFIX + "query");*/
        //in assignment the output string is "Ticket already claimed, delete is not allowed."
        Tickets ticket = aTicket.get();
        if(ticket.isClaimed()) {
            throw new MetrixException(-2, String.format("Ticket with QR Code {%s} already claimed!", qrcode), "/" + VIEW_PREFIX + "query");
        }
        //17. delete the record from DB by passing the ticket object
        repo.delete(ticket);
        return "redirect:/" + VIEW_PREFIX + "list/" + ticket.getEvent().getId();
    }  
}
