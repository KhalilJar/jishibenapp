package com.example.bookkeeping.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookkeepingServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(BookkeepingServerApplication.class, args);
        System.out.println("hello world");
    }

}
