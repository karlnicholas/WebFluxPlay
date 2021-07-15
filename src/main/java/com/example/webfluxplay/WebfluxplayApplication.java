package com.example.webfluxplay;

import com.example.webfluxplay.dao.SomeEntityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class WebfluxplayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxplayApplication.class, args);
    }

    @Autowired
    private SomeEntityDao dao;

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        dao.createTable().subscribe(i->System.out.println("Table created: " + i));
    }

}
