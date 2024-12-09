package ies.jandula.reservaCarritos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import ies.jandula.reservaCarritos.interfaces.IGestroParseo;
import ies.jandula.reservaCarritos.repository.DiasSemanaRepository;
import ies.jandula.reservaCarritos.repository.RecursosRepository;
import ies.jandula.reservaCarritos.repository.TramoHorarioRepository;
import ies.jandula.reservaCarritos.utils.Costantes;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ReservaCarritosApplication implements CommandLineRunner
{
	
	@Autowired
	private IGestroParseo iGestroParseo;
	
	@Autowired
	private RecursosRepository recursosRepository;
	
	@Autowired 
	private TramoHorarioRepository tramoHorarioRepository;
	
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
		
		if(this.recursosRepository.findAll().isEmpty())
		{
			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_RECURSO);
		}
		
		if(this.tramoHorarioRepository.findAll().isEmpty())
		{
			
			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_TRAMOS_HORARIOS);
		}
		
		if(this.diasSemanaRepository.findAll().isEmpty())
		{			
			log.info("No hay datos, cogemos datos del fichero csv");
			this.iGestroParseo.parseaFichero(Costantes.FICHERO_DIAS_SEMANAS);
		}
	}

}
