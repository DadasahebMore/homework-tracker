package com.university.homework;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/** Main Spring Boot application class for Homework Search Microservice */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class HomeworkSearchApplication {

  public static void main(String[] args) {
    SpringApplication.run(HomeworkSearchApplication.class, args);
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Homework Search Service API")
                .version("1.0.0")
                .description(
                    "REST API for searching and filtering homework in university homework library")
                .contact(
                    new Contact()
                        .name("University IT Department")
                        .email("it-support@university.edu")
                        .url("https://university.edu"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
  }
}
