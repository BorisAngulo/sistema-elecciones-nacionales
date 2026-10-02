package bo.edu.electoral;

import bo.edu.electoral.service.MotorElectoralLey026;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static bo.edu.electoral.service.MotorElectoralLey026.Estado.*;

class MotorElectoralTest {
    private final MotorElectoralLey026 motor = new MotorElectoralLey026();
    @Test void mayoriaAbsoluta() {
        var r = motor.evaluar(Map.of(1,51L,2,49L));
        assertEquals(PRIMERA_VUELTA,r.estado()); assertEquals(1,r.ganador());
    }
    @Test void cincuentaEmpatadoNoGana() {
        assertEquals(SEGUNDA_VUELTA,motor.evaluar(Map.of(1,50L,2,50L)).estado());
    }
    @Test void cuarentaExactoYDiezExactosGana() {
        assertEquals(PRIMERA_VUELTA,motor.evaluar(Map.of(1,40L,2,30L,3,30L)).estado());
    }
    @Test void ventajaInsuficiente() {
        assertEquals(SEGUNDA_VUELTA,motor.evaluar(Map.of(1,40L,2,31L,3,29L)).estado());
    }
    @Test void menosDeCuarentaNoGanaAunqueTengaVentaja() {
        assertEquals(SEGUNDA_VUELTA,motor.evaluar(Map.of(1,39L,2,28L,3,20L,4,13L)).estado());
    }
    @Test void cincuentaConVentajaPuedeGanar() {
        assertEquals(PRIMERA_VUELTA,motor.evaluar(Map.of(1,50L,2,40L,3,10L)).estado());
    }
    @Test void sinValidosNoHayGanador() {
        assertEquals(SIN_VOTOS_VALIDOS,motor.evaluar(Map.of()).estado());
        assertNull(motor.evaluar(Map.of(1,0L,2,0L)).ganador());
    }
    @Test void noRedondeaPorcentajesCercanos() {
        assertEquals(SEGUNDA_VUELTA,motor.evaluar(Map.of(1,39999L,2,30000L,3,30001L)).estado());
    }
    @Test void rechazaNegativosYDesbordamiento() {
        assertThrows(IllegalArgumentException.class,()->motor.evaluar(Map.of(1,-1L)));
        assertThrows(ArithmeticException.class,()->motor.evaluar(Map.of(1,Long.MAX_VALUE,2,1L)));
    }
    @Test void comparacionesNoDesbordan() {
        assertEquals(PRIMERA_VUELTA,motor.evaluar(Map.of(1,Long.MAX_VALUE-1,2,1L)).estado());
    }
}

