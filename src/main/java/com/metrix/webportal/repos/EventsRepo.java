package com.metrix.webportal.repos;

import com.metrix.webportal.models.Events;
import org.springframework.data.jpa.repository.JpaRepository;

/*1. extends a JPA class that provide CRUD function on Events object*/
public interface EventsRepo extends JpaRepository<Events, Integer>{
    
}
