package es.iesjandula.reaktor.bookings_server.parsers;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoDiasSemana;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.bookings_server.repository.IDiaSemanaRepository;

@Service
public class ParseoDiasSemanas implements IParseoDiasSemana
{
	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();

		while (scanner.hasNextLine())
		{
			String lineaDelFichero = scanner.nextLine();

			// Dividir la línea por comas
			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			DiaSemana diasSemana = new DiaSemana();
			diasSemana.setDiaSemana(lineaDelFicheroTroceada[0]);
			this.diasSemanaRepository.saveAndFlush(diasSemana);
		}
	}
}
