package ies.jandula.reservaCarritos.iml;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ies.jandula.reservaCarritos.exception.ReservaException;
import ies.jandula.reservaCarritos.interfaces.IParseoProfesor;
import ies.jandula.reservaCarritos.models.Profesor;
import ies.jandula.reservaCarritos.repository.ProfesorRepository;

@Service
public class ParseoProfesor implements IParseoProfesor
{

	@Autowired
	ProfesorRepository profesorRepository;

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
