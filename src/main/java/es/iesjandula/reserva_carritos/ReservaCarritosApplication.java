package es.iesjandula.reserva_carritos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reserva_carritos.interfaces.IGestorParseo;
import es.iesjandula.reserva_carritos.repository.DiasSemanaRepository;
import es.iesjandula.reserva_carritos.repository.ProfesoresRepository;
import es.iesjandula.reserva_carritos.repository.RecursosRepository;
import es.iesjandula.reserva_carritos.repository.TramosHorariosRepository;
import es.iesjandula.reserva_carritos.utils.Costantes;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ReservaCarritosApplication implements CommandLineRunner
{

	@Autowired
	private IGestorParseo iGestroParseo;

	@Autowired
	private RecursosRepository recursosRepository;

	@Autowired
	private TramosHorariosRepository tramosHorariosRepository;

	@Autowired
	private ProfesoresRepository profesoreRepository;

	@Autowired
	private DiasSemanaRepository diasSemanaRepository;

	public static void main(String[] args)
	{
		SpringApplication.run(ReservaCarritosApplication.class, args);
	}

	@Transactional(readOnly = false)
	public void run(String... args) throws Exception
	{
		// TODO Auto-generated method stub

		if (this.recursosRepository.findAll().isEmpty())
		{
			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_RECURSO);
		}

		if (this.tramosHorariosRepository.findAll().isEmpty())
		{

			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_TRAMOS_HORARIOS);
		}

		if (this.diasSemanaRepository.findAll().isEmpty())
		{
			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_DIAS_SEMANAS);
		}
		if (this.profesoreRepository.findAll().isEmpty())
		{
			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_PROFESORES);
		}
	}

}
