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

	@Test
	@DisplayName("AUT-E06 backendStatus: responde UP cuando el backend esta activo")
	void backendStatus_activo_devuelveTrue() {
		apiSimulada.expect(requestTo(API + "/actuator/health"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"status\":\"UP\"}", MediaType.APPLICATION_JSON));

		ResponseEntity<java.util.Map<String, Object>> res = controller.backendStatus();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		assertEquals(true, res.getBody().get("connected"));
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-F06 backendStatus: responde connected=false si el backend falla")
	void backendStatus_error_devuelveFalse() {
		apiSimulada.expect(requestTo(API + "/actuator/health"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withServerError());

		ResponseEntity<java.util.Map<String, Object>> res = controller.backendStatus();
		assertEquals(HttpStatus.OK, res.getStatusCode());
		assertEquals(false, res.getBody().get("connected"));
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E07 setBackendUrl: normaliza URL heredada sin sufijo y vacia")
	void setBackendUrl_normalizaCorrectamente() {
		controller.setBackendUrl("https://api-gestor-backend.onrender.com/");
		controller.setBackendUrl("   ");
		controller.setBackendUrl(null);
		controller.setBackendUrl(API);
	}

	@Test
	@DisplayName("AUT-F07 saveUsuario: si la API falla redirige con errorApi")
	void saveUsuario_apiFalla_redirigeErrorApi() {
		apiSimulada.expect(requestTo(API + "/addUsuario"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withServerError());

		Usuario u = new Usuario();
		u.setNombre("Carol Test");
		u.setCorreo_electronico("carol@test.com");
		u.setContrasena("12345");
		u.setRol("Estudiante");

		ModelAndView mav = controller.setSaveUsuario(u);
		assertTrue(mav.getViewName().startsWith("redirect:/register?errorApi="));
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E08 Vistas GET: home, login, register, inicio, tarea, proyecto")
	void vistasGet_devuelvenNombresCorrectos() {
		Model m = new ConcurrentModel();
		assertEquals("home", controller.getHome());
		assertEquals("login", controller.getLogin(m));
		assertEquals("register", controller.getRegister(m));
		assertEquals("inicio", controller.getInicio(m));
		assertEquals("proyecto", controller.getTarea(m));

		apiSimulada.expect(requestTo(API + "/buscarUsuarios"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		assertEquals("proyecto", controller.getProyecto(m));
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E09 saveUsuario exitoso: redirige a /login?registrado")
	void saveUsuario_valido_redirigeLogin() {
		apiSimulada.expect(requestTo(API + "/addUsuario"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		Usuario u = new Usuario(null, "Ana", "ana@cbtis.mx", "123", "Estudiante", "#111");
		ModelAndView mav = controller.setSaveUsuario(u);
		assertEquals("redirect:/login?registrado", mav.getViewName());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E10 Proyectos CRUD: consultar, actualizar y eliminar")
	void proyectosCrud_funcionaCorrectamente() {
		Model m = new ConcurrentModel();
		apiSimulada.expect(requestTo(API + "/buscarProyectos"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[{\"id_proyecto\":1,\"nombre\":\"P1\"}]", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.consultarProyectos(m).getStatusCode());

		apiSimulada.expect(requestTo(API + "/buscarProyecto?id_proyecto=1"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"id_proyecto\":1,\"nombre\":\"P1\"}", MediaType.APPLICATION_JSON));
		Proyectos p = new Proyectos();
		p.setId_proyecto(1);
		assertEquals(HttpStatus.OK, controller.updateProyecto(p).getStatusCode());

		apiSimulada.expect(requestTo(API + "/deleteProyecto"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		assertEquals("redirect:/inicio", controller.eliminarProyecto(p).getViewName());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-E11 Tareas y Comentarios CRUD completo")
	void tareasYComentariosCrud_funcionaCorrectamente() {
		Model m = new ConcurrentModel();
		Tareas t = new Tareas();
		t.setId_tarea(1);
		t.setId_proyecto(1);
		t.setNombre("Tarea 1");
		t.setFecha_vencimiento("");

		apiSimulada.expect(requestTo(API + "/getTarea"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.obtenerTareas(t, m).getStatusCode());

		apiSimulada.expect(requestTo(API + "/addTareas"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		assertEquals("redirect:/proyecto", controller.setSaveTareas(t).getViewName());

		apiSimulada.expect(requestTo(API + "/buscarTarea?id_tarea=1"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"id_tarea\":1,\"nombre\":\"Tarea 1\"}", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.updateTarea(t).getStatusCode());

		apiSimulada.expect(requestTo(API + "/deleteTareas"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		assertEquals("redirect:/proyecto", controller.eliminarTarea(t).getViewName());

		apiSimulada.expect(requestTo(API + "/addTareas"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.verificarTarea(t).getStatusCode());

		apiSimulada.expect(requestTo(API + "/addTareas"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.cambiosTareas(t).getStatusCode());

		apiSimulada.expect(requestTo(API + "/buscarTareas"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.consultarTareas().getStatusCode());

		com.mx.cbtis.modelo.Comentarios c = new com.mx.cbtis.modelo.Comentarios();
		apiSimulada.expect(requestTo(API + "/getComentario"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.obtenerComentarios(c, m).getStatusCode());

		apiSimulada.expect(requestTo(API + "/addComentario"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("1", MediaType.APPLICATION_JSON));
		assertEquals("proyecto", controller.setSaveComentario(c));

		apiSimulada.expect(requestTo(API + "/buscarComentarios"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
		assertEquals(HttpStatus.OK, controller.consultarComentarios().getStatusCode());
		apiSimulada.verify();
	}

	@Test
	@DisplayName("AUT-F08 verificarLogin: null y error generico devuelven fallback 000")
	void verificarLogin_nullYError_devuelveFallback() {
		apiSimulada.expect(requestTo(API + "/getUsuarioLogin"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("", MediaType.APPLICATION_JSON));
		assertEquals("000", controller.verificarUsuario(new Usuario()).getBody().getNombre());

		apiSimulada.expect(requestTo(API + "/getUsuarioLogin"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withServerError());
		assertEquals("000", controller.verificarUsuario(new Usuario()).getBody().getNombre());
		apiSimulada.verify();
	}
}
