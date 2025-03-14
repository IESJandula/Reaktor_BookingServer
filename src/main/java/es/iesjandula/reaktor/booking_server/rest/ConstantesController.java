package es.iesjandula.reaktor.booking_server.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.booking_server.dto.DtoConstantes;
import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.models.Constantes;
import es.iesjandula.reaktor.booking_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;
import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/bookings/constants", produces =
{ "application/json" })
@RestController
@Log4j2
public class ConstantesController
{
	@Autowired
	private ConstantesRepository constanteRepository;

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> actualizarConstantes()
	{
		try
		{
			List<DtoConstantes> dtoConstantesList = this.constanteRepository.encontrarTodoComoDto();

			return ResponseEntity.ok(dtoConstantesList);
		}
		catch (Exception exception)
		{

			ReservaException reservaException = new ReservaException(Constants.CONSTANTE_NO_ENCONTRADA,
					"Excepción genérica al obtener las costantes", exception);

			log.error("Excepción genérica al obtener las costantes", reservaException);
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

	@PreAuthorize("hasAnyRole('" + BaseConstants.ROLE_ADMINISTRADOR + "', '" + BaseConstants.ROLE_DIRECCION + "')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> actualizarConstantes(@RequestBody(required = true) List<DtoConstantes> dtoConstantesList)
	{
		try
		{
			for (DtoConstantes dtoConstantes : dtoConstantesList)
			{
				Constantes constantes = new Constantes(dtoConstantes.getClave(), dtoConstantes.getValor());

				this.constanteRepository.saveAndFlush(constantes);
			}

			return ResponseEntity.ok().build();
		}
		catch (Exception exception)
		{
			ReservaException reservaException = new ReservaException(Constants.CONSTANTE_NO_ENCONTRADA,
					"Excepción genérica al obtener las costantes", exception);

			log.error("Excepción genérica al actualizar las costantes");
			return ResponseEntity.status(500).body(reservaException.getBodyMesagge());
		}
	}

}
