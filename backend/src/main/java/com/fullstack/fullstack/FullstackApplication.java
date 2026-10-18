package com.fullstack.fullstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal que arranca la aplicación Spring Boot.
 */
@SpringBootApplication  // Agrupa: @Configuration (clase de config), @EnableAutoConfiguration (auto-config), @ComponentScan (escanea paquetes en busca de @Component, @Service, @Repository, etc.).
public class FullstackApplication {

	public static void main(String[] args) {
		SpringApplication.run(FullstackApplication.class, args);
	}
}
