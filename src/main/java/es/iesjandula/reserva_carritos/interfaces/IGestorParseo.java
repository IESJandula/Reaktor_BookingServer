package es.iesjandula.reserva_carritos.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.iesjandula.reserva_carritos.exception.ReservaException;

@Configuration
public interface IGestorParseo
{

	@Bean
	void parseaFichero(String nombreFichero) throws ReservaException;

}
