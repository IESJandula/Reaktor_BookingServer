package es.iesjandula.reaktor.bookings_server.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.bookings_server.dto.DtoConstante;
import es.iesjandula.reaktor.bookings_server.exception.ReservaException;
import es.iesjandula.reaktor.bookings_server.models.Constante;
import es.iesjandula.reaktor.bookings_server.repository.ConstanteRepository;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/constants", produces = {"application/json"})
@RestController
@Log4j2
public class ConstantesController 
{
	@Autowired
	private ConstanteRepository constanteRepository;
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.GET, value = "/constantes")
	public ResponseEntity<?> actualizarConstantes()
	{
		try 
		{
			List<DtoConstante> dtoConstantesList = this.constanteRepository.encontrarTodoComoDto();
			
			return ResponseEntity.ok(dtoConstantesList);
		}
		catch (Exception exception) 
		{

			ReservaException reservaException = new ReservaException(0, "Excepción genérica al obtener las costantes", exception);
			
			log.error("Excepción genérica al obtener las costantes", reservaException);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}
	
	@PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
	@RequestMapping(method = RequestMethod.POST, value = "/constantes")
	public ResponseEntity<?> actualizarConstantes(@RequestBody(required = true) List<DtoConstante> dtoConstantesList)
	{
		try 
		{
			for(DtoConstante dtoConstante : dtoConstantesList) 
			{
				Constante constante = new Constante(dtoConstante.getClave(), dtoConstante.getValor());
				
				this.constanteRepository.saveAndFlush(constante);
			}
			
			return ResponseEntity.ok().build();
		}
		catch (Exception exception) 
		{
			ReservaException reservaException = new ReservaException(0, "Excepción genérica al obtener las costantes", exception);
			
			log.error("Excepción genérica al actualizar las costantes");
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

}
