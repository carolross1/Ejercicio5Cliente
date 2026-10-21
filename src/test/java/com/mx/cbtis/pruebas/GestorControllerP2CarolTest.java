package com.mx.cbtis.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.mx.cbtis.controller.GestorController;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Práctica 2 - Pruebas automáticas (casos de éxito y de fracaso).
 * Responsable: Carol Guadalupe Ríos Ríos - GIDS6102-E
 *
 * Cada prueba crea su propio RestTemplate y lo conecta a MockRestServiceServer,
 * así el controlador nunca llama a la API real (puerto 8081) y las pruebas
 * son repetibles aunque la API o MariaDB estén apagadas.
 */
public class GestorControllerP2CarolTest {

	private static final String API = "http://localhost:8081";

	private MockRestServiceServer apiSimulada;
	private GestorController controller;

	@BeforeEach
	void prepararApiSimulada() {
		RestTemplate restTemplate = new RestTemplate();
		apiSimulada = MockRestServiceServer.createServer(restTemplate);
		controller = new GestorController(restTemplate);
	}

	// ------------------------------------------------------------------
	// CASOS DE ÉXITO
	// ------------------------------------------------------------------

	@Test
	@DisplayName("AUT-E01 consultarUsuarios: la API devuelve los 3 miembros")
	void consultarUsuarios_devuelveTresMiembros() {
		String json = "[{\"id_usuario\":131,\"nombre\":\"Ana Paola Ramírez Vázquez\"},"
				+ "{\"id_usuario\":172,\"nombre\":\"Liliana Gómez Martínez\"},"
				+ "{\"id_usuario\":196,\"nombre\":\"Perla Joceline Martínez Meza\"}]";
		apiSimulada.expect(requestTo(API + "/buscarUsuarios"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

		Model modelo = new ConcurrentModel();
		ResponseEntity<Object> respuesta = controller.consultarUsuarios(modelo);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		List<?> miembros = (List<?>) modelo.getAttribute("listaUsuarios");
		assertNotNull(miembros);
		assertEquals(3, miembros.size());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E02 verificarLogin: credenciales válidas de Liliana")
	void verificarLogin_credencialesLiliana() {
		String json = "{\"id_usuario\":172,\"nombre\":\"Liliana Gómez Martínez\","
				+ "\"correo_electronico\":\"liliana22512@cbtis75.edu.mx\",\"rol\":\"Estudiante\"}";
		apiSimulada.expect(requestTo(API + "/getUsuarioLogin"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.correo_electronico").value("liliana22512@cbtis75.edu.mx"))
				.andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

		Usuario credenciales = new Usuario();
		credenciales.setCorreo_electronico("liliana22512@cbtis75.edu.mx");
		credenciales.setContrasena("ada");

		ResponseEntity<Usuario> respuesta = controller.verificarUsuario(credenciales);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals("Liliana Gómez Martínez", respuesta.getBody().getNombre());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E03 saveProyecto: fechas válidas, se guarda y redirige a /inicio")
	void saveProyecto_fechasValidas_redirigeInicio() {
		apiSimulada.expect(requestTo(API + "/addProyecto"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.nombre").value("Pruebas P2 Carol"))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Proyectos proyecto = new Proyectos();
		proyecto.setNombre("Pruebas P2 Carol");
		proyecto.setDescripcion("Proyecto creado para la práctica 2");
		proyecto.setFecha_inicio("2026-10-15");
		proyecto.setFecha_finalizacion("2026-11-30");
		proyecto.setEstado_proyecto("En proceso");

		ModelAndView vista = controller.setSaveProyecto(proyecto);

		assertEquals("redirect:/inicio", vista.getViewName());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E04 obtenerTareas: el tablero del proyecto 197 recibe sus tareas")
	void obtenerTareas_proyecto197() {
		String json = "[{\"id_tarea\":199,\"nombre\":\"Base de datos\",\"lista\":\"terminado\",\"id_proyecto\":197}]";
		apiSimulada.expect(requestTo(API + "/getTarea"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.id_proyecto").value(197))
				.andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

		Tareas filtro = new Tareas();
		filtro.setId_proyecto(197);
		Model modelo = new ConcurrentModel();

		ResponseEntity<List<Tareas>> respuesta = controller.obtenerTareas(filtro, modelo);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(1, respuesta.getBody().size());
		assertEquals("Base de datos", respuesta.getBody().get(0).getNombre());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E05 verificarTarea: tarea con nombre se envía a la API")
	void verificarTarea_nombreValido_devuelve1() {
		apiSimulada.expect(requestTo(API + "/addTareas"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.nombre").value("Documentar casos P2"))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));

		Tareas tarea = new Tareas();
		tarea.setNombre("Documentar casos P2");
		tarea.setLista("pendientes");
		tarea.setId_proyecto(197);

		ResponseEntity<Integer> respuesta = controller.verificarTarea(tarea);

		assertEquals(HttpStatus.OK, respuesta.getStatusCode());
		assertEquals(1, respuesta.getBody());
		apiSimulada.verify();
	}

	// ------------------------------------------------------------------
	// CASOS DE FRACASO
	// ------------------------------------------------------------------

	@Test
	@DisplayName("AUT-F01 verificarLogin: la API está caída -> 503 controlado")
	void verificarLogin_apiCaida_devuelve503() {
		apiSimulada.expect(requestTo(API + "/getUsuarioLogin"))
				.andRespond(peticion -> {
					throw new ConnectException("Connection refused");
				});

		Usuario credenciales = new Usuario();
		credenciales.setCorreo_electronico("liliana22512@cbtis75.edu.mx");
		credenciales.setContrasena("ada");

		ResponseEntity<Usuario> respuesta = controller.verificarUsuario(credenciales);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-F02 saveUsuario: rol sin seleccionar -> no llama a la API")
	void saveUsuario_rolSinSeleccionar_noLlamaApi() {
		// No se registra ninguna expectativa: si el controlador llamara a
		// /addUsuario, MockRestServiceServer lanzaría "No further requests expected".
		Usuario usuario = new Usuario();
		usuario.setNombre("Carol Guadalupe Ríos Ríos");
		usuario.setCorreo_electronico("carol.rios@utng.edu.mx");
		usuario.setContrasena("p2carol");
		usuario.setRol("0");

		ModelAndView vista = controller.setSaveUsuario(usuario);

		assertEquals("redirect:/register?error", vista.getViewName());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-F03 saveProyecto: fecha fin anterior a inicio -> no se guarda")
	void saveProyecto_fechaFinAnterior_noLlamaApi() {
		Proyectos proyecto = new Proyectos();
		proyecto.setNombre("Proyecto con fechas al revés");
		proyecto.setFecha_inicio("2026-12-01");
		proyecto.setFecha_finalizacion("2026-10-01");
		proyecto.setEstado_proyecto("En proceso");

		ModelAndView vista = controller.setSaveProyecto(proyecto);

		assertEquals("redirect:/inicio?errorFechas", vista.getViewName());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-F04 verificarTarea: nombre vacío -> 400 Bad Request")
	void verificarTarea_nombreVacio_devuelve400() {
		Tareas tarea = new Tareas();
		tarea.setNombre("   ");
		tarea.setLista("pendientes");
		tarea.setId_proyecto(197);

		ResponseEntity<Integer> respuesta = controller.verificarTarea(tarea);

		assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-F05 consultarUsuarios: la API responde 500 -> 503 con mensaje")
	void consultarUsuarios_backend500_devuelve503() {
		apiSimulada.expect(requestTo(API + "/buscarUsuarios"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withServerError());

		Model modelo = new ConcurrentModel();
		ResponseEntity<Object> respuesta = controller.consultarUsuarios(modelo);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
		assertTrue(respuesta.getBody().toString().contains("No se pudieron consultar"));
		apiSimulada.verify();
	}
}
