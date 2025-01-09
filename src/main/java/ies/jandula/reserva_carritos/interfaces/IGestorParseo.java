package ies.jandula.reserva_carritos.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ies.jandula.reserva_carritos.exception.ReservaException;

@Configuration
public interface IGestorParseo
{

	@Bean
	void parseaFichero(String nombreFichero) throws ReservaException;

}
