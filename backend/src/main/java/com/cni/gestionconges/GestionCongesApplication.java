package com.cni.gestionconges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class GestionCongesApplication {

	public static void main(String[] args) {
        SpringApplication.run(GestionCongesApplication.class, args);
	}

}
