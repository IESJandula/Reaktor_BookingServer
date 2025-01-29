package es.iesjandula.reaktor.bookings_server.iml;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.interfaces.IParseoProfesor;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Profesores;
import es.iesjandula.reaktor.bookings_server.repository.IProfesoresRepository;

@Service
public class ParseoProfesor implements IParseoProfesor
{

	@Autowired
	IProfesoresRepository profesorRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();
		List<Profesores> profesores = new ArrayList<Profesores>();
		while (scanner.hasNextLine())
		{

			String lineaDelFichero = scanner.nextLine();

			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			Profesores profesor = new Profesores(
					lineaDelFicheroTroceada[0], lineaDelFicheroTroceada[1], lineaDelFicheroTroceada[2]
			);

			profesores.add(profesor);

		}
		this.profesorRepository.saveAllAndFlush(profesores);
	}

}
