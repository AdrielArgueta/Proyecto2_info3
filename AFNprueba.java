import java.io.*;
import java.util.*;

/**
 * Clase para representar un AFN y convertirlo a AFD.
 * NO debe cambiar los nombres de la clase ni de los métodos existentes.
 */
public class AFNprueba{
    // Lectura AFN.
    private String direccionAFN;
    private String[] alfabeto;
    private int cantidadEstadosAFN;
    private int[] estadosFinalAFN;
    private List<List<Integer>> transicionesLambdaAFN;
    private List<List<List<Integer>>> transicionesEstadosAFN;

    // Algoritmo de clausura-λ.
    private Queue<Set<Integer>> conjuntoCreadosPendientes;                    
    private Map<Integer, Set<Integer>> mapaClausuras;
    private int contadorEstadosClausuras = 1;

    //Variables resultado AFD.
    private String direccionAFD;
    private int cantidadEstadosAFD;
    private List<Integer> estadosFinalAFD;
    private Map<Integer, List<List<Integer>>> transicionesEstadosAFD;

    int cantidadFilasMatrizAFD;
    ArrayList<TransicionAFD>[] transicionesEstadoAFD;

    //Lectura cuerdas.
    private Boolean cuerdaAceptada;

    public AFNprueba(String path){
        this.direccionAFN = path;
        this.transicionesLambdaAFN = new ArrayList<>();
        this.transicionesEstadosAFN = new ArrayList<>();
        this.cuerdaAceptada = false;

        this.conjuntoCreadosPendientes = new LinkedList<>();
        this.mapaClausuras = new HashMap<>();

        this.estadosFinalAFD = new ArrayList<>();


    }

    public boolean accept(String string) {
        return this.cuerdaAceptada;
    }

    public void toAFD(String afdPath){
        this.direccionAFD = afdPath;
        lecturaAFN();
        Set<Integer> estadoInicial = new HashSet<>();
        estadoInicial.add(1);
        clausura_lambda(estadoInicial);
        this.transicionesEstadoAFD = new ArrayList[alfabeto.length];
        for (int i = 0; i < alfabeto.length; i++) {
            this.transicionesEstadoAFD[i] = new ArrayList<>();
        }
        while (!conjuntoCreadosPendientes.isEmpty()) {
            //Obtengo conjunto estado clausura lambda
            Set<Integer> cambioEstado = conjuntoCreadosPendientes.poll();
            //Obtengo el nombre que se le puso a ese conjunto
            int nombreEstadoOrigen = obtenerEstado(cambioEstado);

            for (int i = 0; i < alfabeto.length; i++) {
                //Obtengo el caracter en el indice i del alfabeto
                String caracterCambio = alfabeto[i];
                //Creo el set para el conjunto que buscaremos
                Set<Integer> conjuntoDestino = new HashSet<>();
                //Hago un for para iterar en cada estado individual del conjunto que se obtuvo de la fila de lambda
                for (Integer estado : cambioEstado) {
                    //CReo una lista con las transiciones de la matriz de ese estado.
                    List<Integer> transiciones = transicionesEstadosAFN.get(i).get(estado);
                    //Voy agregando cada transicion al conjunto destino
                    for (Integer siguiente : transiciones) {
                        conjuntoDestino.add(siguiente);
                    }
                }
                //mando a "encolar" el conjunto destino y que tenga su nombre segun lambda
                clausura_lambda(conjuntoDestino);
                Integer nombreEstadoDestino = obtenerEstado(conjuntoDestino);
                //verifico que sea estado final o no.
                if (nombreEstadoDestino != null) {
                    boolean esEstadoFinal = false;
                    for (Integer estado : conjuntoDestino) {
                        if (Arrays.asList(estadosFinalAFN).contains(estado)) {
                            esEstadoFinal = true;
                            break;
                        }
                    }
                    //creo el objeto
                    TransicionAFD transicionCreada = new TransicionAFD(nombreEstadoOrigen, caracterCambio, nombreEstadoDestino, esEstadoFinal);
                    //lo agrego a la matriz que tenemos para AFD
                    transicionesEstadoAFD[i].add(transicionCreada);
                    //agrego el estado si es final al conjunto de estados finales AFD
                    if (esEstadoFinal && !estadosFinalAFD.contains(nombreEstadoDestino)) {
                        estadosFinalAFD.add(nombreEstadoDestino);
                    }
                }
                
            }
        }
        estadosFinales();
        Collections.sort(estadosFinalAFD);

    }
    

    private void lecturaAFN(){
        try(BufferedReader reader = new BufferedReader(new FileReader(direccionAFN))){
            alfabeto = reader.readLine().split(",");
            cantidadEstadosAFN = Integer.parseInt(reader.readLine());
            String[] estadosFinal = reader.readLine().split(",");
            estadosFinalAFN = new int[estadosFinal.length];
            for (int i = 0; i < estadosFinal.length; i++) {
                estadosFinalAFN[i] = Integer.parseInt(estadosFinal[i].trim());
            }

            String[] lambdas = reader.readLine().split(",");
            transicionesLambdaAFN = new ArrayList<>();
            for (String cell : lambdas) {
                List<Integer> transicionLambda = new ArrayList<>();
                for (String p : cell.split(";")) {
                    if (!p.isEmpty()) {
                        transicionLambda.add(Integer.parseInt(p.trim()));
                    }
                }
                transicionesLambdaAFN.add(transicionLambda);
            }

            String line;
            transicionesEstadosAFN = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split(",");
                List<List<Integer>> row = new ArrayList<>();
                for (String cell : cells) {
                    List<Integer> vals = new ArrayList<>();
                    for (String p : cell.split(";")) {
                        if (!p.isEmpty()) {
                            vals.add(Integer.parseInt(p.trim()));
                        }
                    }
                    row.add(vals);
                }
                transicionesEstadosAFN.add(row);
            }
        } catch(IOException e){
            System.err.println("Error leyendo AFN: " + direccionAFN);
        }
    }


    public void clausura_lambda(Set<Integer> conjuntoActual){
        Set<Integer> conjuntoNuevo = new LinkedHashSet<>(conjuntoActual);
        Queue<Integer> cola = new LinkedList<>(conjuntoActual);
        while (!cola.isEmpty()) {
            int estado = cola.poll();
            List<Integer> transiciones = transicionesLambdaAFN.get(estado);
            if (transiciones != null) {
                for (Integer siguiente : transiciones) {
                    if (conjuntoNuevo.add(siguiente)) {
                        cola.add(siguiente);
                    }
                }
            }
        }
        Set<Integer> estadoNuevo = new HashSet<>(conjuntoNuevo);
        if (estadoNuevo.size() == 1 && estadoNuevo.contains(0)) {
            if (!mapaClausuras.containsKey(0)) {
                mapaClausuras.put(0, estadoNuevo);
                conjuntoCreadosPendientes.add(estadoNuevo);
            }
        } else {
            boolean yaExiste = mapaClausuras.values().stream().anyMatch(lista -> new HashSet<>(lista).equals(estadoNuevo));

            if (!yaExiste) {
                mapaClausuras.put(contadorEstadosClausuras, estadoNuevo);
                contadorEstadosClausuras++;
                conjuntoCreadosPendientes.add(estadoNuevo);
            }
        }
        
    }

    public Integer obtenerEstado(Set<Integer> conjuntoABuscar) {
        for (Map.Entry<Integer, Set<Integer>> entry : mapaClausuras.entrySet()) {
            if (entry.getValue().equals(conjuntoABuscar)) {
                return entry.getKey();
            }
        }
        return null;
    }
        
    public void escribirAFD(String nombreArchivo, String path) {
        if (!nombreArchivo.endsWith(".afd")) {
            nombreArchivo += ".afd";
        }
    
        File archivo = new File(path + File.separator + nombreArchivo);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            // Línea 1: alfabeto
            writer.write(String.join(",", alfabeto));
            writer.newLine();
    
            // Línea 2: cantidad de estados
            writer.write(String.valueOf(mapaClausuras.size()));
            writer.newLine();
    
            // Línea 3: estados finales del AFD
            for (int i = 0; i < estadosFinalAFD.size(); i++) {
                writer.write(String.valueOf(estadosFinalAFD.get(i)));
                if (i < estadosFinalAFD.size() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();
    
            // Matriz de transiciones: filas = símbolos, columnas = estados
            int maxEstado = mapaClausuras.keySet().stream().max(Integer::compareTo).orElse(0);
            for (int i = 0; i < alfabeto.length; i++) {
                StringBuilder fila = new StringBuilder();
                for (int estado = 0; estado <= maxEstado; estado++) {
                    boolean encontrado = false;
                    for (TransicionAFD t : transicionesEstadoAFD[i]) {
                        if (t.getEstadoOrigen() == estado) {
                            fila.append(t.getEstadoDestino());
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        fila.append("");
                    }
                    if (estado < maxEstado) {
                        fila.append(",");
                    }
                }
                writer.write(fila.toString());
                writer.newLine();
            }
    
            System.out.println("AFD exportado correctamente en: " + archivo.getAbsolutePath());
    
        } catch (IOException e) {
            System.err.println("Error al escribir AFD en archivo: " + e.getMessage());
        }
    }
    
    
    
    


    public static void main(String[] args) {
        // AFNprueba automata = new AFNprueba("pruebas/afn/prueba1.afn");
        // automata.toAFD("pruebas/afd");
        // automata.escribirAFD("prueba", "pruebas/afd");
        AFNprueba automata = new AFNprueba("tests/afn/lambda_transitions.afn");
        automata.toAFD("pruebas/afd");
        automata.escribirAFD("pruebaDef", "tests/afd");
    }
    
    
    private void estadosFinales() {
        for (Map.Entry<Integer, Set<Integer>> entry : mapaClausuras.entrySet()) {
            Set<Integer> conjunto = entry.getValue();
            for (int finalAFN : estadosFinalAFN) {
                if (conjunto.contains(finalAFN)) {
                    estadosFinalAFD.add(entry.getKey());
                    break;
                }
            }
        }
    }
    

    //***************************************************
    private class TransicionAFD {
        int estadoOrigen;
        String caracter;
        int estadoDestino;
        boolean finalOno;

        public TransicionAFD(int origen, String caracter, int destino, boolean finalOno) {
            this.estadoOrigen = origen;
            this.caracter = caracter;
            this.estadoDestino = destino;
            this.finalOno = finalOno;
        }
        public int getEstadoOrigen(){
            return this.estadoOrigen;
        }
        public int getEstadoDestino(){
            return this.estadoDestino;
        }
        public String getCaracter(){
            return this.caracter;
        }
        public boolean getFinaloNo(){
            return this.finalOno;
        }

    }
    
}
