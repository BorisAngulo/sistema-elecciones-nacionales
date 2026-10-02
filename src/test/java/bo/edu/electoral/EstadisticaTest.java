package bo.edu.electoral;

import bo.edu.electoral.stats.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EstadisticaTest {
    @Test void posicionYDispersionConocidas() {
        double[] datos = {2,4,4,4,5,5,7,9};
        assertEquals(5,MedidasPosicion.media(datos),1e-10);
        assertEquals(4.5,MedidasPosicion.mediana(datos),1e-10);
        assertEquals(4,MedidasDispersion.varianza(datos),1e-10);
        assertEquals(32.0/7,MedidasDispersion.varianza(datos,true),1e-10);
        assertEquals(2,MedidasDispersion.desviacionEstandar(datos),1e-10);
        assertEquals(40,MedidasDispersion.coeficienteVariacion(datos),1e-10);
        assertArrayEquals(new double[]{4},MedidasPosicion.moda(datos));
    }
    @Test void percentilesInterpoladosYNoModificaEntrada() {
        double[] datos = {30,0,20,10};
        assertEquals(7.5,MedidasPosicion.percentil(datos,25));
        assertEquals(0,MedidasPosicion.percentil(datos,0));
        assertEquals(30,MedidasPosicion.percentil(datos,100));
        assertEquals(15,MedidasPosicion.mediana(datos));
        assertArrayEquals(new double[]{30,0,20,10},datos);
        assertEquals(4,MedidasPosicion.mediana(new double[]{9,1,4}));
    }
    @Test void frecuenciasYMultimoda() {
        var f = DistribucionFrecuencia.calcular(new double[]{2,1,2,3,1});
        assertEquals(3,f.length); assertEquals(2,f[0].absoluta());
        assertEquals(0.4,f[0].relativa()); assertEquals(5,f[2].acumulada());
        assertEquals(1,f[2].relativaAcumulada());
        assertArrayEquals(new double[]{1,2},MedidasPosicion.moda(new double[]{1,2,1,2,3}));
        assertEquals(0,MedidasPosicion.moda(new double[]{1,2,3}).length);
    }
    @Test void chebyshev() {
        assertEquals(0.75,MedidasDispersion.chebyshev(2));
        assertArrayEquals(new double[]{1,9},MedidasDispersion.intervaloChebyshev(new double[]{2,4,4,4,5,5,7,9},2));
    }
    @Test void valoresLimite() {
        assertEquals(0,MedidasDispersion.varianza(new double[]{8}));
        assertThrows(IllegalArgumentException.class,()->MedidasPosicion.media(new double[0]));
        assertThrows(IllegalArgumentException.class,()->MedidasPosicion.media(new double[]{Double.NaN}));
        assertThrows(IllegalArgumentException.class,()->MedidasDispersion.varianza(new double[]{1},true));
        assertThrows(IllegalArgumentException.class,()->MedidasDispersion.coeficienteVariacion(new double[]{0,0}));
        assertThrows(IllegalArgumentException.class,()->MedidasDispersion.chebyshev(1));
        assertThrows(IllegalArgumentException.class,()->MedidasPosicion.percentil(new double[]{1},101));
    }
}

