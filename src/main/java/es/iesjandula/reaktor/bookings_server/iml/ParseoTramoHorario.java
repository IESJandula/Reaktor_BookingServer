package es.iesjandula.reaktor.bookings_server.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoTramoHorario;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.TramosHorarios;
import es.iesjandula.reaktor.bookings_server.repository.ITramosHorariosRepository;

@Service
public class ParseoTramoHorario implements IParseoTramoHorario
{

	@Autowired
	private ITramosHorariosRepository tramoHorarioRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{

		scanner.nextLine();

		while (scanner.hasNextLine())
		{

			String lineaDelFichero = scanner.nextLine();

			String[] valores = lineaDelFichero.split(",");

			TramosHorarios tramos = new TramosHorarios();

			tramos.setTramosHorarios(valores[0]);

			this.tramoHorarioRepository.saveAndFlush(tramos);
		}

	}

}
