package ies.jandula.reservaCarritos.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ies.jandula.reservaCarritos.exception.ReservaException;
import ies.jandula.reservaCarritos.interfaces.IParseoRecurso;
import ies.jandula.reservaCarritos.models.Recursos;
import ies.jandula.reservaCarritos.repository.RecursosRepository;

@Service
public class ParseoRecurso implements IParseoRecurso
{
	
	@Autowired
	private RecursosRepository recursosRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		// TODO Auto-generated method stub
		
		scanner.nextLine();
		
		while(scanner.hasNextLine())
		{
			
			String lineaDelFichero = scanner.nextLine();
			
			String [] lineaDelFicheroTroceada = lineaDelFichero.split(",");
			
			Recursos recursos = new Recursos();
			
			recursos.setAulaYCarritos(lineaDelFicheroTroceada[0]);
			
			this.recursosRepository.saveAndFlush(recursos);
		}
		
	}

}
