package es.iesjandula.reaktor.bookings_server.parsers;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoTramoHorario;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.TramoHorario;
import es.iesjandula.reaktor.bookings_server.repository.ITramoHorarioRepository;

@Service
public class ParseoTramoHorario implements IParseoTramoHorario
{
	@Autowired
	private ITramoHorarioRepository tramoHorarioRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();

		while (scanner.hasNextLine())
		{
			String lineaDelFichero = scanner.nextLine();

			String[] valores = lineaDelFichero.split(",");

			TramoHorario tramos = new TramoHorario();

			tramos.setTramoHorario(valores[0]);

			this.tramoHorarioRepository.saveAndFlush(tramos);
		}
	}
}
