package com.academico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainComportamientoTest {

    @Test
    @DisplayName("CP-07: Flujo completo del menú (registrar, estado, responsable, filtro, reporte)")
    void flujoCompletoDelMenu() throws Exception {
        String entrada = String.join("\n",
                "1", "1", "CERTIFICADO", "ALTA", "Necesito certificado", // registrar
                "2", "1", "EN_PROCESO",                                  // cambiar estado
                "3", "1", "1",                                           // asignar responsable
                "4", "EN_PROCESO", "",                                   // filtrar
                "5",                                                     // reporte
                "0") + "\n";

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            // Tiene que ir ANTES de que se cargue la clase Main (su Scanner es static)
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String salida = buffer.toString(StandardCharsets.UTF_8);

        // Evidencia para comparar antes/después (se oculta la IP, que varía según la red)
        String salidaLimpia = salida.replaceAll("H2 Console: http://\\S+", "H2 Console: http://<ip>:8080");
        Files.writeString(Path.of("build", "evidencia_salida_main.txt"), salidaLimpia);

        assertTrue(salida.contains("Solicitud registrada"));
        assertTrue(salida.contains("Estado actualizado."));
        assertTrue(salida.contains("Responsable asignado."));
        assertTrue(salida.contains("Total: 1"));
        assertTrue(salida.contains("Sin responsable: 0"));
        assertTrue(salida.contains("EN_PROCESO: 1"));
    }
}