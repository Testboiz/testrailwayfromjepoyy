package com.indivaragroup.jdt17wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Jdt17wmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(Jdt17wmsApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public java.time.Clock clock() {
		return java.time.Clock.systemUTC();
	}
}
