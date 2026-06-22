package com.example.robotwebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RobotWebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(RobotWebsiteApplication.class, args);
	}

}
