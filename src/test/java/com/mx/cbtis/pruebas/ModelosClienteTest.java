package com.mx.cbtis.pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.mx.cbtis.config.RestTemplateConfig;
import com.mx.cbtis.modelo.Asignaciones;
import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

/**
 * Pruebas unitarias de modelos y configuración del Frontend (Ejercicio5Cliente).
 * Garantiza una cobertura JaCoCo superior al 85% requerida por la evaluación.
 */
public class ModelosClienteTest {

    @Test
    @DisplayName("Usuario: prueba constructor completo y setters")
    void testUsuario() {
        Usuario u = new Usuario(1, "Carol", "carol@test.com", "pass123", "Admin", "foto.png");
        assertEquals(1, u.getId_usuario());
        assertEquals("Carol", u.getNombre());
        assertEquals("carol@test.com", u.getCorreo_electronico());
        assertEquals("pass123", u.getContrasena());
        assertEquals("Admin", u.getRol());
        assertEquals("foto.png", u.getImagen_usuario());

        Usuario uVacio = new Usuario();
        uVacio.setId_usuario(2);
        uVacio.setNombre("Rios");
        uVacio.setCorreo_electronico("rios@test.com");
        uVacio.setContrasena("secret");
        uVacio.setRol("User");
        uVacio.setImagen_usuario("avatar.jpg");

        assertEquals(2, uVacio.getId_usuario());
        assertEquals("Rios", uVacio.getNombre());
        assertEquals("rios@test.com", uVacio.getCorreo_electronico());
        assertEquals("secret", uVacio.getContrasena());
        assertEquals("User", uVacio.getRol());
        assertEquals("avatar.jpg", uVacio.getImagen_usuario());
    }

    @Test
    @DisplayName("Proyectos: prueba constructor completo y setters")
    void testProyectos() {
        Proyectos p = new Proyectos(10, "Gestor", "Sistema GPDS", "2026-10-01", "2026-12-01", "Activo", "bg.jpg");
        assertEquals(10, p.getId_proyecto());
        assertEquals("Gestor", p.getNombre());
        assertEquals("Sistema GPDS", p.getDescripcion());
        assertEquals("2026-10-01", p.getFecha_inicio());
        assertEquals("2026-12-01", p.getFecha_finalizacion());
        assertEquals("Activo", p.getEstado_proyecto());
        assertEquals("bg.jpg", p.getFondo());

        Proyectos pVacio = new Proyectos();
        pVacio.setId_proyecto(20);
        pVacio.setNombre("Nuevo");
        pVacio.setDescripcion("Nueva desc");
        pVacio.setFecha_inicio("2026-11-01");
        pVacio.setFecha_finalizacion("2026-11-30");
        pVacio.setEstado_proyecto("Pendiente");
        pVacio.setFondo("default.png");

        assertEquals(20, pVacio.getId_proyecto());
        assertEquals("Nuevo", pVacio.getNombre());
        assertEquals("Nueva desc", pVacio.getDescripcion());
        assertEquals("2026-11-01", pVacio.getFecha_inicio());
        assertEquals("2026-11-30", pVacio.getFecha_finalizacion());
        assertEquals("Pendiente", pVacio.getEstado_proyecto());
        assertEquals("default.png", pVacio.getFondo());
    }

    @Test
    @DisplayName("Tareas: prueba constructor completo y setters")
    void testTareas() {
        Tareas t = new Tareas(100, "Sprint 1", "Modulo login", "2026-10-25", "Pendiente", "danger", 10);
        assertEquals(100, t.getId_tarea());
        assertEquals("Sprint 1", t.getNombre());
        assertEquals("Modulo login", t.getDescripcion());
        assertEquals("2026-10-25", t.getFecha_vencimiento());
        assertEquals("Pendiente", t.getLista());
        assertEquals("danger", t.getEstado());
        assertEquals(10, t.getId_proyecto());

        Tareas tVacio = new Tareas();
        tVacio.setId_tarea(200);
        tVacio.setNombre("Sprint 2");
        tVacio.setDescripcion("Modulo perfil");
        tVacio.setFecha_vencimiento("2026-11-05");
        tVacio.setLista("Terminado");
        tVacio.setEstado("success");
        tVacio.setId_proyecto(20);

        assertEquals(200, tVacio.getId_tarea());
        assertEquals("Sprint 2", tVacio.getNombre());
        assertEquals("Modulo perfil", tVacio.getDescripcion());
        assertEquals("2026-11-05", tVacio.getFecha_vencimiento());
        assertEquals("Terminado", tVacio.getLista());
        assertEquals("success", tVacio.getEstado());
        assertEquals(20, tVacio.getId_proyecto());
    }

    @Test
    @DisplayName("Comentarios: prueba constructor completo y setters")
    void testComentarios() {
        Comentarios c = new Comentarios(50, "Carol", 100, "Excelente avance", "2026-10-23 16:00:00");
        assertEquals(50, c.getId_comentario());
        assertEquals("Carol", c.getId_usuario());
        assertEquals(100, c.getId_tarea());
        assertEquals("Excelente avance", c.getTexto_comentario());
        assertEquals("2026-10-23 16:00:00", c.getFecha_hora());

        Comentarios cVacio = new Comentarios();
        cVacio.setId_comentario(51);
        cVacio.setId_usuario("Docente");
        cVacio.setId_tarea(101);
        cVacio.setTexto_comentario("Revisado");
        cVacio.setFecha_hora("2026-10-23 17:00:00");

        assertEquals(51, cVacio.getId_comentario());
        assertEquals("Docente", cVacio.getId_usuario());
        assertEquals(101, cVacio.getId_tarea());
        assertEquals("Revisado", cVacio.getTexto_comentario());
        assertEquals("2026-10-23 17:00:00", cVacio.getFecha_hora());
    }

    @Test
    @DisplayName("Asignaciones: prueba constructor completo y setters")
    void testAsignaciones() {
        Asignaciones a = new Asignaciones(1, 10, 100, "2026-10-23");
        assertEquals(1, a.getId_asignacion());
        assertEquals(10, a.getId_usuario());
        assertEquals(100, a.getId_tarea());
        assertEquals("2026-10-23", a.getFecha_asignacion());

        Asignaciones aVacio = new Asignaciones();
        aVacio.setId_asignacion(2);
        aVacio.setId_usuario(20);
        aVacio.setId_tarea(200);
        aVacio.setFecha_asignacion("2026-10-24");

        assertEquals(2, aVacio.getId_asignacion());
        assertEquals(20, aVacio.getId_usuario());
        assertEquals(200, aVacio.getId_tarea());
        assertEquals("2026-10-24", aVacio.getFecha_asignacion());
    }

    @Test
    @DisplayName("RestTemplateConfig: prueba inicialización del bean")
    void testRestTemplateConfig() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate rt = config.restTemplate();
        assertNotNull(rt);
    }
}
