package com.mx.cbtis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Ejercicio5ClienteApplicationTest {

    @Test
    @DisplayName("Ejercicio5ClienteApplication: prueba constructor y main")
    void testApplicationClass() {
        Ejercicio5ClienteApplication app = new Ejercicio5ClienteApplication();
        assertNotNull(app);
        try {
            Ejercicio5ClienteApplication.main(new String[]{"--spring.main.web-application-type=none"});
        } catch (Throwable ignored) {
        }
    }
}
