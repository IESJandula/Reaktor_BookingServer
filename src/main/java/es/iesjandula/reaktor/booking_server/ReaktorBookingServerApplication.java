package es.iesjandula.reaktor.booking_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.booking_server.interfaces.IGestorParseo;
import es.iesjandula.reaktor.booking_server.repository.DiasSemanaRepository;
import es.iesjandula.reaktor.booking_server.repository.ProfesoresRepository;
import es.iesjandula.reaktor.booking_server.repository.RecursosRepository;
import es.iesjandula.reaktor.booking_server.repository.TramosHorariosRepository;
import es.iesjandula.reaktor.booking_server.utils.Costantes;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"es.iesjandula"})
public class ReaktorBookingServerApplication implements CommandLineRunner
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
		SpringApplication.run(ReaktorBookingServerApplication.class, args);
	}

	@Transactional(readOnly = false)
	public void run(String... args) throws Exception
	{
		if (this.recursosRepository.findAll().isEmpty())
		{
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_RECURSO);
		}

		if (this.tramosHorariosRepository.findAll().isEmpty())
		{

			this.iGestroParseo.parseaFichero(Costantes.FICHERO_TRAMOS_HORARIOS);
		}

		if (this.diasSemanaRepository.findAll().isEmpty())
		{
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_DIAS_SEMANAS);
		}
		
		if (this.profesoreRepository.findAll().isEmpty())
		{
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_PROFESORES);
		}
	}

}
