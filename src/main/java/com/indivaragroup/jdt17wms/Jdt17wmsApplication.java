package com.indivaragroup.jdt17wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Jdt17wmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(Jdt17wmsApplication.class, args);
	}

}
