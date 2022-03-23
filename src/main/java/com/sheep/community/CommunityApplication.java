package com.sheep.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * @author sheep
 */
@SpringBootApplication
public class CommunityApplication {
	@PostConstruct
	public void init(){
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
