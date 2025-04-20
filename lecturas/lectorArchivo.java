package lecturas;
import java.io.*;
import java.util.*;

public class lectorArchivo {
    public static void main(String[] args) {
        String archivo = "tests/afn/lambda_transitions.afn";

        String[] alfabeto;
        int cantidadEstados;
        int[] estadoFinal;
        List<List<Integer>> lamda = new ArrayList<>();
        List<List<List<Integer>>> matriz = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            // Línea 1: alfabeto
            alfabeto = br.readLine().split(",");

            // Línea 2: cantidad de estados
            cantidadEstados = Integer.parseInt(br.readLine());

            // Línea 3: estado final (ahora arreglo)
            String[] estadoFinalStr = br.readLine().split(",");
            estadoFinal = new int[estadoFinalStr.length];
            for (int i = 0; i < estadoFinalStr.length; i++) {
                estadoFinal[i] = Integer.parseInt(estadoFinalStr[i]);
            }

            // Línea 4: lamda (lista de listas)
            String[] lamdaStr = br.readLine().split(",");
            for (String celda : lamdaStr) {
                List<Integer> grupo = new ArrayList<>();
                for (String parte : celda.split(";")) {
                    if (!parte.isEmpty()) {
                        grupo.add(Integer.parseInt(parte));
                    }
                }
                lamda.add(grupo);
            }

            // Matriz desde la línea 5 en adelante
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] celdas = linea.split(",");

                List<List<Integer>> fila = new ArrayList<>();
                for (String celda : celdas) {
                    List<Integer> valores = new ArrayList<>();
                    String[] partes = celda.split(";");
                    for (String parte : partes) {
                        if (!parte.isEmpty()) {
                            valores.add(Integer.parseInt(parte));
                        }
                    }
                    fila.add(valores);
                }

                matriz.add(fila);
            }

            // Verificación
            System.out.println("Alfabeto: " + Arrays.toString(alfabeto));
            System.out.println("Cantidad de Estados: " + cantidadEstados);
            System.out.println("Estado Final: " + Arrays.toString(estadoFinal));
            System.out.println("Lamda:");
            for (List<Integer> grupo : lamda) {
                System.out.println(grupo);
            }
            System.out.println("Matriz:");
            for (List<List<Integer>> fila : matriz) {
                System.out.print("[ ");
                for (List<Integer> celda : fila) {
                    System.out.print(celda + " ");
                }
                System.out.println("]");
            }

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
