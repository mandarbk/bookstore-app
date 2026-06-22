package com.bookstore.eureka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class BookstoreEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookstoreEurekaServerApplication.class, args);
	}

}
