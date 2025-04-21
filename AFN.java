import java.io.*;
import java.util.*;

public class AFN{
    //Variables para Lectura de AFN.
    private String direccionAFN;
    private String[] alfabeto;
    private int cantidadEstadosAFN;
    private int[] estadosFinalAFN;
    private List<List<Integer>> transicionesLambdaAFN;
    private List<List<List<Integer>>> transicionesEstadosAFN;

    //Variables para Algoritmo de clausura-λ.
    private Queue<Set<Integer>> conjuntoCreadosPendientes;                    
    private Map<Integer, Set<Integer>> mapaClausuras;
    private int contadorEstadosClausuras = 1;

    //Variables para resultado de AFD.
    private String direccionAFD;
    private int cantidadEstadosAFD;
    private List<Integer> estadosFinalAFD;
    int cantidadFilasMatrizAFD;
    ArrayList<TransicionAFD>[] transicionesEstadoAFD;

    /**
     * Constructor de la clase AFN.
     *
     * Descripción:
     * Inicializa todas las variables necesarias para todos los métodos.
     *
     * Parámetros:
     * @param path - direccion donde se encuentra el AFN.
     *
     */
    public AFN(String path){
        this.direccionAFN = path;
        this.transicionesLambdaAFN = new ArrayList<>();
        this.transicionesEstadosAFN = new ArrayList<>();

        this.conjuntoCreadosPendientes = new LinkedList<>();
        this.mapaClausuras = new HashMap<>();

        this.estadosFinalAFD = new ArrayList<>();
    }
    
    //********************************** LECTURA CUERDAS AFD/AFN **********************************

    /**
     * Nombre del método: accept
     *
     * Descripción: 
     * Verificamos si una cadena es aceptada por el AFD 
     * Con este metodo simulamos la ejecucion de automata, sobre la cadena de entrada
     * Para que sea considerada aceptada, la cadena debe terminar en un estado final despues de reccorerla
     *
     * Parámetros:
     * @param string - cuerda que se quiere verificar.     *
     * Valor de retorno:
     * @return boolean - true o false dependiendo de la validación de la cuerda.
     */
    public boolean accept(String string) {
        if(transicionesEstadoAFD == null || transicionesEstadoAFD.length != alfabeto.length){
            toAFD("salida.afd");
        }
        int estadoActual = 1;

        for (int i=0; i<string.length(); i++){
            char simbolo = string.charAt(i);
            int idxSimbolo = getIndiceSimbolo(simbolo);
            if(idxSimbolo == -1)return false; 

            boolean transicionEncontrada = false;
            for(TransicionAFD t: transicionesEstadoAFD[idxSimbolo]){
                if(t.estadoOrigen == estadoActual){
                    estadoActual = t.estadoDestino;
                    transicionEncontrada = true;
                    break;
                }
            }
            if(!transicionEncontrada){
                estadoActual = 0;
                break;
            }
        }
        return estadosFinalAFD.contains(estadoActual);
    }

    /**
     * Nombre del método: getIndiceSimbolo
     *
     * Descripción: 
     * Metodo auxiliar para obtener el indice de un simbolo en el alfabeto del automata
     *
     * Parámetros:
     * @param simbolo - caracter de la cuerda que se esta verificando.
     *
     * Valor de retorno:
     * @return int - indicedel simbolo en el alfabeto.
     */
    private int getIndiceSimbolo(char simbolo){
        for(int i = 0; i<alfabeto.length; i++){
            if(alfabeto[i].equals(String.valueOf(simbolo))){
                return i;
            }
        }
        return -1;
    }

    //********************************** CONVERSION AFD **********************************

    /**
     * Nombre del método: toAFD
     *
     * Descripción: 
     * Se encarga de realizar el algotimos de cambio(Estado,caracter).
     *
     * Parámetros:
     * @param String afdPath - direccion de guardado para afd.
     */
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
            Set<Integer> cambioEstado = conjuntoCreadosPendientes.poll();
            int nombreEstadoOrigen = obtenerEstado(cambioEstado);
            for (int i = 0; i < alfabeto.length; i++) {
                String caracterCambio = alfabeto[i];
                Set<Integer> conjuntoDestino = new HashSet<>();
                for (Integer estado : cambioEstado) {
                    List<Integer> transiciones = transicionesEstadosAFN.get(i).get(estado);
                    for (Integer siguiente : transiciones) {
                        conjuntoDestino.add(siguiente);
                    }
                }
                clausura_lambda(conjuntoDestino);
                Integer nombreEstadoDestino = obtenerEstado(conjuntoDestino);
                if (nombreEstadoDestino != null) {
                    boolean esEstadoFinal = false;
                    for (Integer estado : conjuntoDestino) {
                        if (Arrays.asList(estadosFinalAFN).contains(estado)) {
                            esEstadoFinal = true;
                            break;
                        }
                    }
                    TransicionAFD transicionCreada = new TransicionAFD(nombreEstadoOrigen, caracterCambio, nombreEstadoDestino, esEstadoFinal);
                    transicionesEstadoAFD[i].add(transicionCreada);
                    if (esEstadoFinal && !estadosFinalAFD.contains(nombreEstadoDestino)) {
                        estadosFinalAFD.add(nombreEstadoDestino);
                    }
                }
                
            }
        }
        estadosFinales();
        Collections.sort(estadosFinalAFD);
    }
    
    /**
     * Nombre del método: lecturaAFN
     *
     * Descripción: 
     * Realiza la lectura del archivo .afn guardando cada linea es su variable corresponciente para su posterior uso.
     */
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

    /**
     * Nombre del método: clausura_lambda
     *
     * Descripción: 
     * Realiza el algoritmo de clausura lambda a cada conjunto de estado enviado.
     *
     * Parámetros:
     * @param conjuntoActual - conjunto de estado que se desea obtener su clausura lambda.
     *
     */

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

    /**
     * Nombre del método: obtenerEstado
     *
     * Descripción: 
     * Se busca dentro del mapa de clausura de lambda el nombre que se le fue asignado.
     *
     * Parámetros:
     * @param conjuntoABuscar - conjutno de estado que deseas obtener su nombre en el mapa de clausura de lambda.
     *
     * Valor de retorno:
     * @return Integer - nombre asignado en el metodo claurua_lambda.
     */
    public Integer obtenerEstado(Set<Integer> conjuntoABuscar) {
        for (Map.Entry<Integer, Set<Integer>> entry : mapaClausuras.entrySet()) {
            if (entry.getValue().equals(conjuntoABuscar)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Nombre del método: escribirAFD
     *
     * Descripción: 
     * Genera el archivo .afd con todos los datos que se solicitan en el proyecto.
     *
     * Parámetros:
     * @param nombreArchivo - el nombre que tendra el archivo .afd.
     * @param path - donde se guardara el archivo .afd.
     * 
     */
    public void escribirAFD(String nombreArchivo, String path) {
        if (!nombreArchivo.endsWith(".afd")) {
            nombreArchivo += ".afd";
        }
    
        File archivo = new File(path + File.separator + nombreArchivo);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write(String.join(",", alfabeto));
            writer.newLine();
            writer.write(String.valueOf(mapaClausuras.size()));
            writer.newLine();
            for (int i = 0; i < estadosFinalAFD.size(); i++) {
                writer.write(String.valueOf(estadosFinalAFD.get(i)));
                if (i < estadosFinalAFD.size() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();
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
            System.out.println("AFD exportado correctamente en: ");
            System.out.println(archivo.getAbsolutePath());
    
        } catch (IOException e) {
            System.out.println("Error al escribir AFD en archivo: " + e.getMessage());
        }
    }

    /**
     * Nombre del método: estadosFInales
     *
     * Descripción: 
     * Verifica que conjuntos de claurura de lambda son estados finales y hace una copia de los nombres para la impresion posterior.
     * 
     */
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

    /**
     * Nombre de la clase: TransicionAFD
     *
     * Descripción: 
     * Representa cada transicion nueva hecha por el algoritmo completo.
     * 
     */
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

    //********************************** MAIN **********************************

    /**
     * Nombre del método: main
     *
     * Descripción: 
     * Metodo principal que se ejecutara segun lo que necesite el usuario.
     *
     * Parámetros:
     * @param args - arreglo de argumentos que se leen en la linea de ejecución.
     *
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            AFN automata = new AFN(args[0]);
            BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
            System.out.println();
            System.out.println("=======================================================================================");
            System.out.println("                 BIENVENIDO A NUESTRO PROYECTO NO.2 DE INFORMATICA 3 :D");
            System.out.println("=======================================================================================");
            System.out.println("Ingresa las cuerdas que quieres validar en tu AFN (presiona ENTER vacío para salir):");
            System.out.println();
            
            while (true) {
                System.out.print("~ ");
                String linea = lector.readLine();
                if (linea == null || linea.isEmpty()) break;
                
                boolean aceptada = automata.accept(linea);
                System.out.println(aceptada ? "La cuerda \""+ linea +"\" es ACEPTADA" : "La cuerda \""+ linea +"\" es RECHAZADA");
            }
            
            System.out.println();
            System.out.println("Terminando programa...");
            System.out.println("Gracias por usar nuestro programa :D");
            System.out.println();
            System.out.println("=======================================================================================");
            System.out.println();
            System.out.println("CREDITOS:");
            System.out.println();
            System.out.println("Programa desarrollado por:");
            System.out.println();
            System.out.println("  Adriel Levi Argueta Caal         - 24003171");
            System.out.println("  Maria Claudia Lainfiesta Herrera - 24000149");
            System.out.println("  Jeancarlo de León                - 24002596");
            System.out.println();
            System.out.println("=======================================================================================");            
        } else if (args.length == 3 && args[1].equals("-to-afd")) {
            System.out.println();
            System.out.println("=======================================================================================");
            System.out.println("                 BIENVENIDO A NUESTRO PROYECTO NO.2 DE INFORMATICA 3 :D");
            System.out.println("=======================================================================================");
            System.out.println();
            AFN automata = new AFN(args[0]);
            String ruta = args[0];
            int punto = ruta.lastIndexOf('.');
            int barra = ruta.lastIndexOf('/');

            String nombre = ruta.substring(barra + 1, punto);
            automata.toAFD(args[2]);
            automata.escribirAFD(nombre,args[2]);
            System.out.println();
            System.out.println("Gracias por usar nuestro programa :D");
            System.out.println();
            System.out.println("=======================================================================================");
            System.out.println();
            System.out.println("CREDITOS:");
            System.out.println();
            System.out.println("Programa desarrollado por:");
            System.out.println();
            System.out.println("  Adriel Levi Argueta Caal         - 24003171");
            System.out.println("  Maria Claudia Lainfiesta Herrera - 24000149");
            System.out.println("  Jeancarlo de León                - 24002596");
            System.out.println();
            System.out.println("======================================================================================="); 
    
        } else {
            System.out.println("-------------------------------------------");
            System.out.println(" OPCIONES DE USO");
            System.out.println("-------------------------------------------");
            System.out.println();
            System.out.println("1. VALIDAR CADENAS CON UN ARCHIVO .afn");
            System.out.println("-------------------------------------------");
            System.out.println("Comando:");
            System.out.println("  java AFN path/archivo.afn");
            System.out.println();
            System.out.println("Descripción:");
            System.out.println("  Proporciona la ruta completa del archivo .afn que deseas utilizar.");
            System.out.println("  El programa te permitirá ingresar cuerdas para verificar si son aceptadas por el AFN.");
            System.out.println("  Para salir, presiona ENTER sin escribir ninguna cuerda.");
            System.out.println();
            System.out.println("2. CONVERTIR UN ARCHIVO .afn A .afd");
            System.out.println("-------------------------------------------");
            System.out.println("Comando:");
            System.out.println("  java AFN path/archivo.afn -to-afd path/");
            System.out.println();
            System.out.println("Descripción:");
            System.out.println("  Proporciona la ruta completa del archivo .afn de entrada.");
            System.out.println("  Luego, indica solamente la carpeta de destino para guardar el archivo .afd.");
            System.out.println("  RECUERDA: El archivo .afd tendrá el mismo nombre que el archivo .afn original.");
            System.out.println();
        }
    }
        
}
