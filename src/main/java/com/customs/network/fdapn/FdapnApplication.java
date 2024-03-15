package com.customs.network.fdapn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FdapnApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdapnApplication.class, args);
	}

}
