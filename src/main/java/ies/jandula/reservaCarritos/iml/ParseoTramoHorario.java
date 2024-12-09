package ies.jandula.reservaCarritos.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ies.jandula.reservaCarritos.exception.ReservaException;
import ies.jandula.reservaCarritos.interfaces.IParseoTramoHorario;
import ies.jandula.reservaCarritos.models.TramosHorarios;
import ies.jandula.reservaCarritos.repository.TramoHorarioRepository;

@Service
public class ParseoTramoHorario implements IParseoTramoHorario
{
	
	@Autowired
	private TramoHorarioRepository tramoHorarioRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException 
	{
		
		scanner.nextLine();
		
		while(scanner.hasNextLine())
		{
			
			String lineaDelFichero = scanner.nextLine();
			
			String [] valores = lineaDelFichero.split(",");
			
			TramosHorarios tramos = new TramosHorarios();
			
			tramos.setTramosHorarios(valores[0]);
			
			this.tramoHorarioRepository.saveAndFlush(tramos);
		}
		
	}

}
