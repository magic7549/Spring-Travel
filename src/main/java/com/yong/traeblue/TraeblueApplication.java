package com.yong.traeblue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TraeblueApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraeblueApplication.class, args);
	}

}
