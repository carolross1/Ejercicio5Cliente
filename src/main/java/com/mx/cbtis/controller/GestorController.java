package com.mx.cbtis.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

@Controller
public class GestorController {

	/**
	 * Corrección F-01: un solo RestTemplate inyectado por Spring
	 * (antes se creaba "new RestTemplate()" en cada método).
	 */
	private final RestTemplate template;

	public GestorController(RestTemplate template) {
		this.template = template;
	}

	/** Mensaje controlado que se devuelve cuando la API (8081) no responde. */
	private static Map<String, String> mensajeError(String mensaje) {
		Map<String, String> error = new HashMap<String, String>();
		error.put("mensaje", mensaje);
		return error;
	}

	private static boolean vacio(String valor) {
		return valor == null || valor.trim().isEmpty();
	}

	@GetMapping("/")
	public String getHome() {
		return "home";
	}

	@GetMapping("/tutorial")
	public String getTutorial() {
		return "tutorial";
	}

	@GetMapping("/manual")
	public String getManual() {
		return "manual";
	}

	@GetMapping("/login")
	public String getLogin() {
		return "login";
	}

	@GetMapping("/register")
	public String getRegister(Model modelo) {

		Usuario usuario = new Usuario();
		modelo.addAttribute("usuario", usuario);

		return "register";
	}

	@PostMapping("/saveUsuario")
	public ModelAndView setSaveUsuario(@ModelAttribute Usuario usuario) {

		// Corrección F-03: no se envía a la API un usuario incompleto
		// o con el rol "Seleccione una opción" (valor "0").
		if (vacio(usuario.getNombre()) || vacio(usuario.getCorreo_electronico())
				|| vacio(usuario.getContrasena()) || vacio(usuario.getRol())
				|| "0".equals(usuario.getRol())) {
			return new ModelAndView("redirect:/register?error");
		}

		String urlservicebd = "http://localhost:8081/addUsuario";
		template.postForEntity(urlservicebd, usuario, Integer.class);

		return new ModelAndView("redirect:/login");
	}

	@GetMapping("/inicio")
	public String getInicio(Model modelo) {

		Proyectos proyectos = new Proyectos();
		modelo.addAttribute("proyectos", proyectos);

		return "inicio";
	}

	@PostMapping("/saveProyecto")
	public ModelAndView setSaveProyecto(@ModelAttribute Proyectos proyecto) {

		// Corrección F-04: la fecha de finalización no puede ser anterior a la de inicio.
		if (!vacio(proyecto.getFecha_inicio()) && !vacio(proyecto.getFecha_finalizacion())) {
			try {
				LocalDate inicio = LocalDate.parse(proyecto.getFecha_inicio());
				LocalDate fin = LocalDate.parse(proyecto.getFecha_finalizacion());
				if (fin.isBefore(inicio)) {
					return new ModelAndView("redirect:/inicio?errorFechas");
				}
			} catch (DateTimeParseException e) {
				return new ModelAndView("redirect:/inicio?errorFechas");
			}
		}

		String urlservicebd = "http://localhost:8081/addProyecto";
		template.postForEntity(urlservicebd, proyecto, Integer.class);

		return new ModelAndView("redirect:/inicio");
	}

	@PostMapping("/consultarProyectos")
	public ResponseEntity<Object> consultarProyectos(Model modelo) {

		String urlservicebd = "http://localhost:8081/buscarProyectos";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Proyectos[] response = template.postForObject(urlservicebd, headers, Proyectos[].class);
		List<Proyectos> proyectos = Arrays.asList(response);
		modelo.addAttribute("listaProyectos", proyectos);

		ResponseEntity<Object> objeto = new ResponseEntity<Object>(proyectos, HttpStatusCode.valueOf(200));

		return objeto;
	}

	@PostMapping("/eliminarProyecto")
	public ModelAndView eliminarProyecto(@RequestBody Proyectos proyecto) {
		System.out.println("El id del proyecto es: " + proyecto.getId_proyecto());
		String urlservicebd = "http://localhost:8081/deleteProyecto";
		HttpEntity<Proyectos> request = new HttpEntity<Proyectos>(proyecto);
		template.exchange(urlservicebd, HttpMethod.DELETE, request, Integer.class);

		return new ModelAndView("redirect:/inicio");
	}

	@PostMapping("/actualizarProyecto")
	public ResponseEntity<Object> updateProyecto(@RequestBody Proyectos proyecto) {
		Map<String, Integer> params = new HashMap<String, Integer>();
		params.put("id_proyecto", proyecto.getId_proyecto());
		String urlservicebd = "http://localhost:8081/buscarProyecto?id_proyecto={id_proyecto}";
		Proyectos proyectos = template.getForObject(urlservicebd, Proyectos.class, params);

		ResponseEntity<Object> objeto = new ResponseEntity<Object>(proyectos, HttpStatusCode.valueOf(200));

		return objeto;
	}

	@PostMapping("/verificarLogin")
	public ResponseEntity<Usuario> verificarUsuario(@RequestBody Usuario paramUser) {
		String urlservicebd = "http://localhost:8081/getUsuarioLogin";
		try {
			return template.postForEntity(urlservicebd, paramUser, Usuario.class);
		} catch (ResourceAccessException e) {
			// Corrección F-02: la API está detenida; se responde 503 para que
			// login.js muestre "No se pudo conectar con el servidor".
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}
	}

	@PostMapping("/consultarUsuarios")
	public ResponseEntity<Object> consultarUsuarios(Model modelo) {

		String urlservicebd = "http://localhost:8081/buscarUsuarios";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			Usuario[] response = template.postForObject(urlservicebd, headers, Usuario[].class);
			List<Usuario> usuarios = Arrays.asList(response);
			modelo.addAttribute("listaUsuarios", usuarios);
			return new ResponseEntity<Object>(usuarios, HttpStatusCode.valueOf(200));
		} catch (RestClientException e) {
			// Corrección F-06: un error 500 de la API o la API caída ya no
			// se propaga como 500 sin control; se responde 503 con mensaje.
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body((Object) mensajeError("No se pudieron consultar los miembros del proyecto"));
		}
	}

	@PostMapping("/obtenerTareas")
	public ResponseEntity<List<Tareas>> obtenerTareas(@RequestBody Tareas idProyecto, Model modelo) {
		String urlservicebd = "http://localhost:8081/getTarea";
		Tareas[] response = template.postForObject(urlservicebd, idProyecto, Tareas[].class);
		List<Tareas> listaTareas = Arrays.asList(response);
		modelo.addAttribute("tareas", listaTareas);
		return ResponseEntity.ok(listaTareas);
	}

	@PostMapping("/obtenerComentarios")
	public ResponseEntity<List<Comentarios>> obtenerComentarios(@RequestBody Comentarios idComentario, Model modelo) {
		String urlservicebd = "http://localhost:8081/getComentario";
		Comentarios[] response = template.postForObject(urlservicebd, idComentario, Comentarios[].class);
		List<Comentarios> listaComentarios = Arrays.asList(response);
		modelo.addAttribute("comentarios", listaComentarios);
		return ResponseEntity.ok(listaComentarios);
	}

	// Liliana
	@GetMapping("/proyecto")
	public String getProyecto(Model modelo) {
		Tareas tarea = new Tareas();
		modelo.addAttribute("tarea", tarea);

		Comentarios comentario = new Comentarios();
		modelo.addAttribute("comentarios", comentario);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String urlservicebdUsuario = "http://localhost:8081/buscarUsuarios";
		Usuario[] responseUsuario = template.postForObject(urlservicebdUsuario, headers, Usuario[].class);
		List<Usuario> usuarios = Arrays.asList(responseUsuario);
		modelo.addAttribute("listaUsuarios", usuarios);

		return "proyecto";
	}

	@GetMapping("/tarea")
	public String getTarea(Model modelo) {
		Tareas tareas = new Tareas();
		modelo.addAttribute("tareas", tareas);

		return "proyecto";
	}

	@PostMapping("/saveTarea")
	public ModelAndView setSaveTareas(@ModelAttribute Tareas tareas) {

		// Corrección F-07 (INT-08): el formulario envía fecha_vencimiento = "" cuando
		// no se elige fecha y MariaDB rechaza "" en una columna DATE (500 en /addTareas).
		// Una fecha vacía se envía como null.
		if (vacio(tareas.getFecha_vencimiento())) {
			tareas.setFecha_vencimiento(null);
		}

		String urlservicebd = "http://localhost:8081/addTareas";
		template.postForEntity(urlservicebd, tareas, Integer.class);
		ModelAndView mav = new ModelAndView("redirect:/proyecto");
		mav.addObject("id_proyecto", tareas.getId_proyecto());

		return mav;
	}

	@PostMapping("/saveComentario")
	public String setSaveComentario(@RequestBody Comentarios comentarios) {

		String urlservicebd = "http://localhost:8081/addComentario";
		template.postForEntity(urlservicebd, comentarios, Integer.class);

		return "proyecto";
	}

	@PostMapping("/actualizarTarea")
	public ResponseEntity<Object> updateTarea(@RequestBody Tareas tarea) {
		Map<String, Integer> params = new HashMap<String, Integer>();
		params.put("id_tarea", tarea.getId_tarea());
		String urlservicebd = "http://localhost:8081/buscarTarea?id_tarea={id_tarea}";
		Tareas tareas = template.getForObject(urlservicebd, Tareas.class, params);

		ResponseEntity<Object> objeto = new ResponseEntity<Object>(tareas, HttpStatusCode.valueOf(200));

		return objeto;
	}

	@PostMapping("/eliminarTarea")
	public ModelAndView eliminarTarea(@RequestBody Tareas tareas) {
		System.out.println("El id d de la tarea es: " + tareas.getId_tarea());
		String urlservicebd = "http://localhost:8081/deleteTareas";
		HttpEntity<Tareas> request = new HttpEntity<Tareas>(tareas);
		template.exchange(urlservicebd, HttpMethod.DELETE, request, Integer.class);

		return new ModelAndView("redirect:/proyecto");
	}

	@PostMapping("/verificarTarea")
	public ResponseEntity<Integer> verificarTarea(@RequestBody Tareas paramName) {
		// Corrección F-05: no se crean tareas sin nombre.
		if (vacio(paramName.getNombre())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(0);
		}
		String urlservicebd = "http://localhost:8081/addTareas";
		ResponseEntity<Integer> response = template.postForEntity(urlservicebd, paramName, Integer.class);
		return response;
	}

	@PostMapping("/cambiosTareas")
	public ResponseEntity<Integer> cambiosTareas(@RequestBody Tareas paramDatos) {
		String urlservicebd = "http://localhost:8081/addTareas";
		ResponseEntity<Integer> response = template.postForEntity(urlservicebd, paramDatos, Integer.class);
		return response;
	}

	@PostMapping("/consultarTareas")
	public ResponseEntity<Object> consultarTareas() {
		String urlservicebd = "http://localhost:8081/buscarTareas";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Tareas[] response = template.postForObject(urlservicebd, headers, Tareas[].class);
		List<Tareas> tareas = Arrays.asList(response);
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(tareas, HttpStatusCode.valueOf(200));

		return objeto;
	}

	@PostMapping("/consultarComentarios")
	public ResponseEntity<Object> consultarComentarios() {
		String urlservicebd = "http://localhost:8081/buscarComentarios";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Comentarios[] response = template.postForObject(urlservicebd, headers, Comentarios[].class);
		List<Comentarios> comentarios = Arrays.asList(response);
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(comentarios, HttpStatusCode.valueOf(200));

		return objeto;
	}
}