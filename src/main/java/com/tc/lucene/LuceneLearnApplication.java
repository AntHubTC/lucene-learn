package com.tc.lucene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LuceneLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuceneLearnApplication.class, args);
    }

}
