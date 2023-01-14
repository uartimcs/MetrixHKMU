package com.metrix.webportal.repos;

import com.metrix.webportal.models.Tickets;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/*1. extends a JPA class that provide CRUD function on Tickets object*/
public interface TicketsRepo extends JpaRepository<Tickets, Integer>{
    //1. Annotation for abstract function, filter the tickets records in DB against QR Code
    @Query("SELECT t FROM Tickets t WHERE t.qrcode = :qrcode")
    //2. Abstract function the return a Optional<Tickets> and accept a String parameter "qrcode"
    Optional <Tickets> getTicketByQrCode(String qrcode);
}