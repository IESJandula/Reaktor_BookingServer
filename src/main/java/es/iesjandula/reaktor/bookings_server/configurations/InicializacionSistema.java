package es.iesjandula.reaktor.bookings_server.configurations;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.models.Constante;
import es.iesjandula.reaktor.bookings_server.repository.ConstanteRepository;
import es.iesjandula.reaktor.bookings_server.utils.Costantes;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class InicializacionSistema 
{
	
	@Autowired
	private ConstanteRepository constanteRepository;
	
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String modoDdl;
	
	@Value("${" + Costantes.PARAM_YAML_RESERVAS_FIJAS + "}")
	private String reservasFijas;

	@Value("${" + Costantes.PARAM_YAML_RESERVAS_PUNTUALES + "}")
	private String reservasPuntuales;
	
	/**
	 * Este método se encarga de inicializar el sistema
	 * ya sea en el entorno de desarrollo o ejecutando JAR
	 */
	@PostConstruct
	public void inicializarSistema()
	{
		
		if(Costantes.MODO_DDL_CREATE.equalsIgnoreCase(this.modoDdl)) 
		{
			this.inicializarSistemaConConstantes();
			
		}
	}
	
	/**
	 * Este método se encarga de inicializar el sistema con las constantes siempre que estemos 
	 * creando la base de datos ya sea en el entorno de desarrollo o ejecutando JAR
	 */
	private void inicializarSistemaConConstantes()
	{
		this.cargarPropiedad(Costantes.TABLA_CONST_RESERVAS_FIJAS, this.reservasFijas) ;
		this.cargarPropiedad(Costantes.TABLA_CONST_RESERVAS_PUNTUALES, this.reservasPuntuales) ;
	}
	
	private void cargarPropiedad(String key, String value)
	{
		// Verificamos si tiene algún valor
        Optional<Constante> property = this.constanteRepository.findById(key) ;
        
        // Si está vacío, lo seteamos con el valor del YAML
        if (property.isEmpty())
        {
        	Constante constante = new Constante() ;
        	
            constante.setClave(key) ;
            constante.setValor(value) ;
            
            // Almacenamos la constante en BBDD
            this.constanteRepository.save(constante) ;
        }
    }

}
