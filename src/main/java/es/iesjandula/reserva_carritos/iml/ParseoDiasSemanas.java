package es.iesjandula.reserva_carritos.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reserva_carritos.exception.ReservaException;
import es.iesjandula.reserva_carritos.interfaces.IParseoDiasSemana;
import es.iesjandula.reserva_carritos.models.reservas_fijas.DiasSemana;
import es.iesjandula.reserva_carritos.repository.DiasSemanaRepository;

@Service
public class ParseoDiasSemanas implements IParseoDiasSemana
{
	@Autowired
	private DiasSemanaRepository diasSemanaRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();

		while (scanner.hasNextLine())
		{
			String lineaDelFichero = scanner.nextLine();

			// Dividir la línea por comas
			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			DiasSemana diasSemana = new DiasSemana();
			diasSemana.setDiasDeLaSemana(lineaDelFicheroTroceada[0]);
			this.diasSemanaRepository.saveAndFlush(diasSemana);
		}
	}

}
