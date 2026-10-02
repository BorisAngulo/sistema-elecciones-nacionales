package bo.edu.electoral;

import bo.edu.electoral.service.ValidadorActaService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ValidadorActaTest {
    private final ValidadorActaService v = new ValidadorActaService();
    @Test void actaConsistente() { assertDoesNotThrow(()->v.validar("HABILITADA",false,100,80,5,5,70,80)); }
    @Test void mesaSinAsistencia() { assertDoesNotThrow(()->v.validar("HABILITADA",false,100,0,0,0,0,0)); }
    @Test void estadoYUnicidad() {
        for (String estado : new String[]{"ANULADA","COMPUTADA","OTRO"})
            assertThrows(IllegalArgumentException.class,()->v.validar(estado,false,10,0,0,0,0,0));
        assertThrows(IllegalArgumentException.class,()->v.validar("HABILITADA",true,10,0,0,0,0,0));
    }
    @Test void conteosInconsistentes() {
        assertThrows(IllegalArgumentException.class,()->v.validar("HABILITADA",false,5,6,0,0,6,6));
        assertThrows(IllegalArgumentException.class,()->v.validar("HABILITADA",false,10,6,0,0,5,5));
        assertThrows(IllegalArgumentException.class,()->v.validar("HABILITADA",false,10,6,0,0,5,6));
        assertThrows(IllegalArgumentException.class,()->v.validar("HABILITADA",false,10,0,-1,1,0,0));
    }
}

