package es.iesjandula.reaktor.booking_server.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.iesjandula.reaktor.booking_server.exception.ReservaException;

@Configuration
public interface IGestorParseo
{

	@Bean
	void parseaFichero(String nombreFichero) throws ReservaException;

}
