package com.apidynamics.test.client_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class ClientDemoApplication {

	public static void main(String[] args) {
//		Security.setProperty("crypto.policy", "unlimited");
		SpringApplication.run(ClientDemoApplication.class, args);
	}

}
