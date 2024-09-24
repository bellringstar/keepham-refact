package com.ssafy.keepham;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
@EnableKafka
@OpenAPIDefinition(servers = {@Server(url = "https://i9c104.p.ssafy.io", description = "server")
        , @Server(url = "http://localhost:8080", description = "local")})
public class KeephamApplication {

    @PostConstruct
    public void started()   {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(KeephamApplication.class, args);
    }

}
