package com.listatarefas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ListaTarefasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListaTarefasApplication.class, args);
    }
}
