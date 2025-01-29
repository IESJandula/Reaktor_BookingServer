package es.iesjandula.reaktor.bookings_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.bookings_server.interfaces.IGestorParseo;
import es.iesjandula.reaktor.bookings_server.repository.IDiasSemanaRepository;
import es.iesjandula.reaktor.bookings_server.repository.IProfesoresRepository;
import es.iesjandula.reaktor.bookings_server.repository.IRecursosRepository;
import es.iesjandula.reaktor.bookings_server.repository.ITramosHorariosRepository;
import es.iesjandula.reaktor.bookings_server.utils.Costantes;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"es.iesjandula"})
public class ReaktorBookingServerApplication implements CommandLineRunner
{

	@Autowired
	private IGestorParseo iGestorParseo;

	@Autowired
	private IRecursosRepository recursosRepository;

	@Autowired
	private ITramosHorariosRepository tramosHorariosRepository;

	@Autowired
	private IProfesoresRepository profesoreRepository;

	@Autowired
	private IDiasSemanaRepository diasSemanaRepository;

	public static void main(String[] args)
	{
		SpringApplication.run(ReaktorBookingServerApplication.class, args);
	}

	@Transactional(readOnly = false)
	public void run(String... args) throws Exception
	{
		if (this.recursosRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Costantes.FICHERO_RECURSO);
		}

		if (this.tramosHorariosRepository.findAll().isEmpty())
		{

			this.iGestorParseo.parseaFichero(Costantes.FICHERO_TRAMOS_HORARIOS);
		}

		if (this.diasSemanaRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Costantes.FICHERO_DIAS_SEMANAS);
		}
		
		if (this.profesoreRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Costantes.FICHERO_PROFESORES);
		}
	}

}
