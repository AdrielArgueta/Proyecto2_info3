import java.io.*;

/**
 * Clase separada para probar la conversión de AFN a AFD.
 */
public class AFNConversionTest {
    public static void main(String[] args) {
        // Rutas de prueba (modifica según tu entorno)
        String afnPath = "pruebas/afn/prueba1.afn";    // archivo de entrada AFN
        String afdPath = "pruebas/afd/prueba1.afd";     // archivo de salida AFD

        try {
            // Instanciar y convertir
            AFN automata = new AFN(afnPath);
            automata.toAFD(afdPath);
            System.out.println("Conversión completada.");
            System.out.println("Archivo AFD generado en: " + afdPath);
        } catch (Exception e) {
            System.err.println("Error durante la conversión de AFN a AFD:");
            e.printStackTrace();
        }
    }
}
