package es.iesjandula.reaktor.bookings_server.utils;

public class Costantes
{
	
	/*********************************************************/
	/*********************** Errores *************************/
	/*********************************************************/

	public final static int STD_CODE_ERROR = 500;
	
	/*********************************************************/
	/************************ Ficheros ***********************/
	/*********************************************************/

	public static final String FICHERO_RECURSO = "recursos.csv";

	public static final String FICHERO_TRAMOS_HORARIOS = "tramosHorarios.csv";

	public static final String FICHERO_DIAS_SEMANAS = "diasSemana.csv";

	public static final String FICHERO_PROFESORES = "profesor.csv";
	
	
	/*********************************************************/
	/****************** Modo DDL - Create ********************/
	/*********************************************************/
	
	public static final String MODO_DDL_CREATE = "create" ;
	
	/*********************************************************/
	/******************** Tabla Constantes *******************/
	/*********************************************************/
	
	public static final String TABLA_CONST_RESERVAS_FIJAS = "Reservas fijas";

	public static final String TABLA_CONST_RESERVAS_PUNTUALES = "Reservas puntuales";
	
	/*********************************************************/
	/******************* Parámetros YAML *********************/
	/*********************************************************/
	
	/** Constante - Parámetros YAML - Reservas fijas */
	public static final String PARAM_YAML_RESERVAS_FIJAS = "reaktor.constantes.reservasFijas" ;
	
	/** Constante - Parámetros YAML - Reservas puntuales */
	public static final String PARAM_YAML_RESERVAS_PUNTUALES = "reaktor.constantes.reservasPuntuales" ;


}
