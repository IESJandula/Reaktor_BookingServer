package es.iesjandula.reaktor.bookings_server.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoRecurso;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursosPrevios;
import es.iesjandula.reaktor.bookings_server.repository.IRecursosRepository;

@Service
public class ParseoRecurso implements IParseoRecurso
{

	@Autowired
	private IRecursosRepository recursosRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		// TODO Auto-generated method stub

		scanner.nextLine();

		while (scanner.hasNextLine())
		{

			String lineaDelFichero = scanner.nextLine();

			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			RecursosPrevios recursos = new RecursosPrevios();

			recursos.setAulaYCarritos(lineaDelFicheroTroceada[0]);

			this.recursosRepository.saveAndFlush(recursos);
		}

	}

}
