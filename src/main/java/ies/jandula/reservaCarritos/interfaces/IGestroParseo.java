package ies.jandula.reservaCarritos.interfaces;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ies.jandula.reservaCarritos.exception.ReservaException;

@Configuration
public interface IGestroParseo {
	
	@Bean
	void parseaFichero(String nombreFichero) throws ReservaException;

}
