package com.customs.network.fdapn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@Configuration
public class FdapnApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdapnApplication.class, args);
	}

}
