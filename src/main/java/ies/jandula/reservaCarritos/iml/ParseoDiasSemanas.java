package ies.jandula.reservaCarritos.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ies.jandula.reservaCarritos.exception.ReservaException;
import ies.jandula.reservaCarritos.interfaces.IParseoDiasSemana;
import ies.jandula.reservaCarritos.models.DiasSemana;
import ies.jandula.reservaCarritos.repository.DiasSemanaRepository;

@Service
public class ParseoDiasSemanas implements IParseoDiasSemana
{
	@Autowired
	private DiasSemanaRepository diasSemanaRepository;	
	
	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException 
	{
	    scanner.nextLine();
	    
	    while (scanner.hasNextLine()) 
	    {
	        String lineaDelFichero = scanner.nextLine();
	        
	        // Dividir la línea por comas
	        String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");
	        
	        // Se valida que hay datos suficientes en la línea
	        
//	        if (lineaDelFicheroTroceada.length > 0) 
//	        {
//	            for (int i = 2; i < lineaDelFicheroTroceada.length; i++)
//	            {
//	                String diaSemana = lineaDelFicheroTroceada[i];
//	                
//	                // Verifica si el día de la semana no está vacío
//	                if (!diaSemana.isEmpty()) 
//	                {
//	                    
//	                    
//	                    // Guarda en la base de datos si no existe
//	                    if (!this.diasSemanaRepository.existsById(diaSemana))
//	                    {
//	                        this.diasSemanaRepository.saveAndFlush(diasSemana);
//	                    }
//	                }
//	            }
//	        }
	        DiasSemana diasSemana = new DiasSemana();
            diasSemana.setDiasDeLaSemana(lineaDelFicheroTroceada[0]);
            this.diasSemanaRepository.saveAndFlush(diasSemana);
	    }
	}


}
