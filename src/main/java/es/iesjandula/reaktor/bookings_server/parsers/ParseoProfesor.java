package es.iesjandula.reaktor.bookings_server.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoProfesor;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Profesor;
import es.iesjandula.reaktor.bookings_server.repository.IProfesorRepository;

@Service
public class ParseoProfesor implements IParseoProfesor
{
	@Autowired
	IProfesorRepository profesorRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();
		List<Profesor> profesores = new ArrayList<Profesor>();
		while (scanner.hasNextLine())
		{
			String lineaDelFichero = scanner.nextLine();

			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			Profesor profesor = new Profesor(lineaDelFicheroTroceada[0], lineaDelFicheroTroceada[1],
					lineaDelFicheroTroceada[2]);

			profesores.add(profesor);
		}

		this.profesorRepository.saveAllAndFlush(profesores);
	}
}
