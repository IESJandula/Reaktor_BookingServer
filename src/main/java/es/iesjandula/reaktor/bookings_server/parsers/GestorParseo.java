package es.iesjandula.reaktor.bookings_server.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IGestorParseo;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoDiasSemana;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoProfesor;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoTramoHorario;
import es.iesjandula.reaktor.bookings_server.utils.Constants;

@Service
public class GestorParseo implements IGestorParseo
{

	@Autowired
	private IParseoTramoHorario parseoTramoHorario;

	@Autowired
	private IParseoProfesor iParseoProfesor;

	@Autowired
	private IParseoDiasSemana parseoDiasSemana;

	@Override
	public void parseaFichero(String nombreFichero) throws ReservaException
	{
		switch (nombreFichero)
		{	
			case Constants.FICHERO_TRAMOS_HORARIOS:
				Scanner scannerTramosHorarios = this.abrirFichero(nombreFichero);
	
				this.parseoTramoHorario.parseaFichero(scannerTramosHorarios);
	
				scannerTramosHorarios.close();
				break;
			case Constants.FICHERO_DIAS_SEMANAS:
				Scanner scannerDiasSemana = this.abrirFichero(nombreFichero);
	
				this.parseoDiasSemana.parseaFichero(scannerDiasSemana);
	
				scannerDiasSemana.close();
				break;
	
			case Constants.FICHERO_PROFESORES:
				Scanner scannerProfesor = this.abrirFichero(nombreFichero);
	
				this.iParseoProfesor.parseaFichero(scannerProfesor);
	
				scannerProfesor.close();
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
			File fichero = this.getFileFromResource(nombreFichero);

			return new Scanner(fichero);
		} catch (FileNotFoundException fileNotFoundException)
		{
			throw new ReservaException(5, "Fichero " + nombreFichero + " no encontrado!", fileNotFoundException);
		} catch (URISyntaxException uriSyntaxException)
		{
			throw new ReservaException(6, "Fichero " + nombreFichero + " no encontrado!", uriSyntaxException);
		}

	}

	private File getFileFromResource(String nombreFichero) throws URISyntaxException
	{
		ClassLoader classLoader = getClass().getClassLoader();

		URL resource = classLoader.getResource(nombreFichero);

		if (resource == null)
		{
			throw new IllegalArgumentException("Fichero no encontrado! " + nombreFichero);
		}

		return new File(resource.toURI());
	}

}
