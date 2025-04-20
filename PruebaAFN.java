import java.io.*;
import java.util.*;

/**
 * Clase para representar un AFN y convertirlo a AFD.
 * NO debe cambiar los nombres de la clase ni de los métodos existentes.
 */
public class AFN{
    // Lectura AFN.
    private String direccionAFN;
    private String[] alfabeto;
    private int cantidadEstadosAFN;
    private String[] estadosFinalAFN;
    private List<List<String>> transicionesLambda;
    private List<List<List<String>>> transicionesEstados;

    // Algoritmo de clausura-λ.
    private Queue<ArrayList<String>> pendientes = new LinkedList<>();            
    private Queue<ArrayList<String>> procesados = new LinkedList<>();           
    private Map<Integer, ArrayList<String>> mapaClausuras = new HashMap<>();
    private int contadorClausuras = 1;

    // Variables resultado AFD
    private String direccionAFD;
    private int cantidadEstadosAFD;
    private List<String> estadosFinalAFD;
    private Map<Integer, List<List<String>>> transicionesEstadosAFD;

    // Lectura cuerdas (proyecto 1)
    private Boolean cuerdaAceptada;

    /**
     * Constructor: recibe path al archivo .AFN
     */
    public AFN(String path){
        this.direccionAFN       = path;
        this.transicionesLambda = new ArrayList<>();
        this.transicionesEstados = new ArrayList<>();
        this.cuerdaAceptada     = false;
    }

    /**
     * Estub del método accept, implementa la evaluación de la cuerda
     */
    public boolean accept(String cuerda){
        lecturaAFN();
        Set<String> estadosIniciales = new LinkedHashSet<>(transicionesLambda.get(1)); // Cla -- lamda del estado 1 
        estadosIniciales.add("1");

        estadosIniciales = clausura_lambda(estadosIniciales); //Buscamos mas cla--lam desde el estado 1 

        Set<String> estadosActuales = new LinkedHashSet<>(estadosIniciales);

        for(int j = 0; j < cuerda.length(); j++){ //Usamos for para recorrer la cuerda simbolo a simbolo
            char simbolo = cuerda.charAt(j);
            int indexSimbolo = getIndiceSimbolo(simbolo);
            if(indexSimbolo == -1)return false; //Simbolo invalido

            Set<String> nuevoEstados = new LinkedHashSet<>();
            for(String estado : estadosActuales){
                int e = Integer.parseInt(estado);
                nuevoEstados.addAll(transicionesEstados.get(indexSimbolo).get(e));
            }
            estadosActuales = clausura_lambda(nuevoEstados);
        }

        for(String estado : estadosActuales){//ciclo en busca de estados finales. 
            for(String finalEstado : estadosFinalAFN){
                if(estado.equals(finalEstado))
                return true;
            }
        }
        return false;
    }

    /**
     * Convierte este AFN en un AFD, llenando:
     * - cantidadEstadosAFD
     * - estadosFinalAFD
     * - transicionesEstadosAFD
     * y luego escribe el archivo .afd en direccionAFD
     */
    public void toAFD(String afdPath){
        this.direccionAFD = afdPath;
        lecturaAFN();

        // Inicializar estructuras AFD
        transicionesEstadosAFD = new HashMap<>();
        estadosFinalAFD = new ArrayList<>();

        // Paso inicial: clausura-λ del estado 1
        ArrayList<String> inicio = new ArrayList<>();
        inicio.add("1");
        String nombreInicial = clausura_lambda(inicio);

        // BFS sobre los nombres de estados AFD
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(nombreInicial);
        visited.add(nombreInicial);

        // Recorrer todos los nuevos estados
        while(!queue.isEmpty()){
            String origen = queue.poll();
            int keyOrigen = Integer.parseInt(origen);
            ArrayList<String> conjunto = mapaClausuras.get(keyOrigen);

            List<List<String>> fila = new ArrayList<>();
            // Para cada símbolo del alfabeto
            for(int i=0; i<alfabeto.length; i++){
                Set<String> destSet = new LinkedHashSet<>();
                for(String est : conjunto){
                    int idx = Integer.parseInt(est);
                    destSet.addAll(transicionesEstados.get(i).get(idx));
                }
                // Clausura-λ del destino
                ArrayList<String> destList = new ArrayList<>(destSet);
                String nombreDest = clausura_lambda(destList);
                fila.add(destList);

                if(!visited.contains(nombreDest)){
                    visited.add(nombreDest);
                    queue.add(nombreDest);
                }
            }
            transicionesEstadosAFD.put(keyOrigen, fila);
        }

        // Cantidad de estados AFD
        cantidadEstadosAFD = mapaClausuras.size();

        // Estados finales AFD
        Set<String> finales = new LinkedHashSet<>();
        for(Map.Entry<Integer, ArrayList<String>> e : mapaClausuras.entrySet()){
            for(String est : e.getValue()){
                if(Arrays.asList(estadosFinalAFN).contains(est)){
                    finales.add(String.valueOf(e.getKey()));
                    break;
                }
            }
        }
        estadosFinalAFD.addAll(finales);

        // Escribir archivo .afd
        escribirAFD();
    }

    private void lecturaAFN(){
        try(BufferedReader reader = new BufferedReader(new FileReader(direccionAFN))){
            alfabeto = reader.readLine().split(",");
            cantidadEstadosAFN = Integer.parseInt(reader.readLine());
            estadosFinalAFN = reader.readLine().split(",");

            // Transiciones lambda
            String[] lambdas = reader.readLine().split(",");
            for(String cell : lambdas){
                List<String> group = new ArrayList<>();
                for(String p : cell.split(";")) if(!p.isEmpty()) group.add(p);
                transicionesLambda.add(group);
            }
            // Transiciones normales
            String line;
            while((line = reader.readLine())!=null){
                String[] cells = line.split(",");
                List<List<String>> row = new ArrayList<>();
                for(String cell : cells){
                    List<String> vals = new ArrayList<>();
                    for(String p : cell.split(";")) if(!p.isEmpty()) vals.add(p);
                    row.add(vals);
                }
                transicionesEstados.add(row);
            }
        } catch(IOException e){
            System.err.println("Error leyendo AFN: " + direccionAFN);
            e.printStackTrace();
        }
    }


    public String clausura_lambda(ArrayList<String> conjuntoActual){
        Set<String> setNuevo = new LinkedHashSet<>();
        for(String est : conjuntoActual){
            setNuevo.addAll(transicionesLambda.get(Integer.parseInt(est)));
        }
        ArrayList<String> listaNueva = new ArrayList<>(setNuevo);
        // Verificar en procesados y pendientes
        for(ArrayList<String> l : procesados){ if(l.equals(listaNueva)) return obtenerNombreEstado(l);}        
        for(ArrayList<String> l : pendientes){ if(l.equals(listaNueva)) return obtenerNombreEstado(l);}        
        // Nuevo
        pendientes.add(listaNueva);
        mapaClausuras.put(contadorClausuras, listaNueva);
        contadorClausuras++;
        return String.valueOf(contadorClausuras-1);
    }

    private String obtenerNombreEstado(ArrayList<String> conjunto){
        for(Map.Entry<Integer, ArrayList<String>> e : mapaClausuras.entrySet()){
            if(e.getValue().equals(conjunto)) return String.valueOf(e.getKey());
        }
        return null;
    }

    private void escribirAFD(){
        String path = direccionAFD.endsWith(".afd") ? direccionAFD : direccionAFD + ".afd";
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(path))){
            // Alfabeto
            bw.write(String.join(",", alfabeto)); bw.newLine();
            // Cantidad de estados
            bw.write(String.valueOf(cantidadEstadosAFD)); bw.newLine();
            // Estados finales
            bw.write(String.join(",", estadosFinalAFD)); bw.newLine();
            // Transiciones, ordenadas por estado
            List<Integer> estados = new ArrayList<>(transicionesEstadosAFD.keySet());
            Collections.sort(estados);
            for(int estado : estados){
                List<List<String>> fila = transicionesEstadosAFD.get(estado);
                for(int i=0; i<alfabeto.length; i++){
                    String dests = String.join(";", fila.get(i));
                    bw.write(estado + "," + alfabeto[i] + "," + dests);
                    bw.newLine();
                }
            }
        } catch(IOException e){
            System.err.println("Error escribiendo AFD: " + path);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        // TODO: implementar invocación
    }
}
