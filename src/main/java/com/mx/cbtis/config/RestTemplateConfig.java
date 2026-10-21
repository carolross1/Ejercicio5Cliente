package com.mx.cbtis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Corrección F-01 (Práctica 2 - Carol Guadalupe Ríos Ríos).
 * El controlador creaba "new RestTemplate()" dentro de cada método, por lo que
 * MockRestServiceServer nunca interceptaba las peticiones en las pruebas.
 * Ahora existe un único RestTemplate administrado por Spring que se inyecta
 * en GestorController y que las pruebas pueden reemplazar por un simulador.
 */
@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
