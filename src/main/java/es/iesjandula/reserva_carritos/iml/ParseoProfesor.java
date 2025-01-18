package es.iesjandula.reserva_carritos.iml;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reserva_carritos.exception.ReservaException;
import es.iesjandula.reserva_carritos.interfaces.IParseoProfesor;
import es.iesjandula.reserva_carritos.models.reservas_fijas.Profesores;
import es.iesjandula.reserva_carritos.repository.ProfesoresRepository;

@Service
public class ParseoProfesor implements IParseoProfesor
{

	@Autowired
	ProfesoresRepository profesorRepository;

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
