package es.iesjandula.reaktor.bookings_server.parsers;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoRecurso;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoPrevio;
import es.iesjandula.reaktor.bookings_server.repository.IRecursoPrevioRepository;

@Service
public class ParseoRecurso implements IParseoRecurso
{
	@Autowired
	private IRecursoPrevioRepository recursosRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();

		while (scanner.hasNextLine())
		{

			String lineaDelFichero = scanner.nextLine();

			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			RecursoPrevio recursos = new RecursoPrevio();

			recursos.setId(lineaDelFicheroTroceada[0]);

			this.recursosRepository.saveAndFlush(recursos);
		}
	}
}
