package ies.jandula.reservaCarritos.iml;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ies.jandula.reservaCarritos.exception.ReservaException;
import ies.jandula.reservaCarritos.interfaces.IGestroParseo;
import ies.jandula.reservaCarritos.interfaces.IParseoDiasSemana;
import ies.jandula.reservaCarritos.interfaces.IParseoRecurso;
import ies.jandula.reservaCarritos.interfaces.IParseoTramoHorario;
import ies.jandula.reservaCarritos.utils.Costantes;

@Service
public class GestorParseo implements IGestroParseo
{
	
	@Autowired
	private IParseoRecurso parseoRecurso;
	
	@Autowired
	private IParseoTramoHorario parseoTramoHorario;
	
	@Autowired
	private IParseoDiasSemana parseoDiasSemana;

	@Override
	public void parseaFichero(String nombreFichero) throws ReservaException 
	{
		// TODO Auto-generated method stub
		
		switch(nombreFichero) 
		{
			case Costantes.FICHERO_RECURSO:
				Scanner scannerRecurso = this.abrirFichero(nombreFichero);
				
				this.parseoRecurso.parseaFichero(scannerRecurso);
				
				scannerRecurso.close();
				break;
				
			case Costantes.FICHERO_TRAMOS_HORARIOS:
				Scanner scannerTramosHorarios = this.abrirFichero(nombreFichero);
				
				this.parseoTramoHorario.parseaFichero(scannerTramosHorarios);
				
				scannerTramosHorarios.close();
				break;
			case Costantes.FICHERO_DIAS_SEMANAS:
				Scanner scannerDiasSemana = this.abrirFichero(nombreFichero);
				
				this.parseoDiasSemana.parseaFichero(scannerDiasSemana);
				
				scannerDiasSemana.close();
				break;
				
			default:
				throw new ReservaException(1, "Fichero" + nombreFichero + "no encontrado");
		}
		
	}
	
	private Scanner abrirFichero(String nombreFichero) throws ReservaException
	{
		try
		{
			// Get file from resource
			File fichero = this.getFileFromResource(nombreFichero) ;
			
			return new Scanner(fichero) ;
		}
		catch (FileNotFoundException fileNotFoundException)
		{
			throw new ReservaException(5, "Fichero " + nombreFichero + " no encontrado!", fileNotFoundException) ;
		}
		catch (URISyntaxException uriSyntaxException)
		{
			throw new ReservaException(6, "Fichero " + nombreFichero + " no encontrado!", uriSyntaxException) ;
		}
		
	}
	
	private File getFileFromResource(String nombreFichero) throws URISyntaxException
	{
        ClassLoader classLoader = getClass().getClassLoader() ;
        
        URL resource = classLoader.getResource(nombreFichero) ;
        
        if (resource == null)
        {
            throw new IllegalArgumentException("Fichero no encontrado! " + nombreFichero) ;
        }

        return new File(resource.toURI()) ;
    }

}
