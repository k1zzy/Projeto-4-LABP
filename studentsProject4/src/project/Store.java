package project;

/**
 * Classe que representa uma Store com um stock de produtos e um número de balcões.
 * Uma Store é uma loja que processa clientes e vende produtos.
 * 
 * @author Rodrigo Afonso (61839)
 * @version 1.0
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class Store {
    private Counter[] counters;
    private final Product[] productCatalog;
    private double totalSalesAmount;
    private StringBuilder sb;
    public static final String EOL = System.lineSeparator();
    
    /** 
     * Construtor da classe Store que representa uma loja que recebe
     * um ficheiro para ler o stock e a quantidade de balcões e um StringBuilder que representa o log.
     * 
     * @throws IllegalArgumentException se o ficheiro não existir ou se o formato do ficheiro for inválido
     * @param fileName ficheiro com o stock
     * @param sb StringBuilder para o log
     */
    public Store(String fileName, StringBuilder sb) {
        this.sb = sb;
        String[] catalog;
        try {
           catalog = readStore(fileName); // le o ficheiro e guarda o conteudo num array
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        try {
            this.counters = new Counter[Integer.parseInt(catalog[0])]; // cria os balcoes com o tamanho indicado
            this.productCatalog = new Product[Integer.parseInt(catalog[1])]; // cria o catalogo de produtos com o tamanho indicado
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid stock file format");
        }
        fillProducts(catalog); // preenche o catalogo com os produtos do ficheiro
        iniciateCounters(); // inicia os balcoes
    }
    
    
    /** 
     * Lê o ficheiro com o stock e o número de balcões a abrir e
     * devolve um array de strings com o conteúdo.
     * 
     * @param fileName nome do ficheiro
     * @throws IllegalArgumentException se o ficheiro não existir
     * @return String[] array de strings com o conteúdo do ficheiro
     */
    private static String[] readStore(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            Scanner sc = new Scanner(new File(fileName));
            // le o ficheiro e guarda o conteudo num StringBuilder
            // enquanto houver linhas no ficheiro
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                sb.append(" ");
            }
            sc.close();
        } 
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Stock file does not exist");
        }
        // divide o conteudo do ficheiro por espaços e devolve-o num array
        return sb.toString().split(" ");
    }
    
    
    /** 
     * Preenche o catalogo de produtos com os produtos do ficheiro de stock.
     * 
     * @param catalog array de strings com o conteúdo do ficheiro
     */
    private void fillProducts(String[] catalog) {
        try {
            for (int i = 0; i < productCatalog.length; i++) {
                // como coloquei o conteudo do ficheiro num array de strings todos de seguida
                // o indice do array que pretendo é:
                // i*2+2+i (funciona cheguei la por tentativa e erro)
                productCatalog[i] = new Product(catalog[i*2+2+i], Double.parseDouble(catalog[i*2+3+i]), Integer.parseInt(catalog[i*2+4+i]));
            }
        }
        // se tiver elementos a mais ou a menos
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid catalog format");
        }
    }
    
    /** 
     * Inicia o número de balcões indicado no ficheiro de stock com os parametros iniciais.
     */
    private void iniciateCounters() {
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new Counter(i, 0, this.sb);
        }
    }
    
    
    /** 
     * Lê o ficheiro de eventos e processa-os, atribuindo os clientes aos balcões que terminam mais cedo,
     * ou abrindo balcões se solicitado.
     * No final, processa todos os balcões até que todos os clientes tenham sido processados,
     * calcula o total de vendas e escreve tudo no log.
     * 
     * @param fileName nome do ficheiro de eventos
     * @throws FileNotFoundException se o ficheiro de eventos não existir
     */
    public void processEvents(String fileName) throws FileNotFoundException {
        int currentTime = 0; // tempo atual
        int currentUserId = 0; // id do cliente atual
        int currentLine = 0; // linha atual do ficheiro
        int clientProcessingDuration = 0; // tempo de processamento do cliente
        Scanner sc = null; // scanner para ler o ficheiro
        try {
            // tenta abrir o ficheiro
            sc = new Scanner(new File(fileName));
        }
        catch (FileNotFoundException e) {
            //sc.close();
            throw new FileNotFoundException("Events file does not exist");
        }
        
        while(sc.hasNextLine()) {
            currentLine++;
            String[] event = sc.nextLine().split(" ");
            clientProcessingDuration = 0;
            
            if (event[1].equals("CLIENT")) { // se o evento for do tipo CLIENT
                Product[] cart = new Product[event.length - 3];
                try {
                    // tenta converter os parametros do evento para inteiros
                    currentTime = Integer.parseInt(event[0]);
                    currentUserId = Integer.parseInt(event[2]);
                }
                catch (Exception e) {
                    sc.close();
                    throw new IllegalArgumentException("Invalid event format in line " + currentLine);
                }
                // le todos os itens do carrinho do cliente e compara-os com o
                // catalago da loja, se existir adiciona-os ao contador de itens
                for(int i = 3; i < event.length; i++) {
                    for (int j = 0; j < productCatalog.length; j++) {
                        if (event[i].equals(productCatalog[j].getProdCode())) {
                            clientProcessingDuration += productCatalog[j].getProdProcessingDuration();
                            cart[i-3] = productCatalog[j];
                            break;
                        }
                    }
                }
                processAllCountersUntil(processClient(new Client(currentUserId, Arrays.copyOf(cart, cart.length), currentTime, clientProcessingDuration)));
            }
            else if(event[1].equals("COUNTER")) { // se o evento for do tipo COUNTER
                try {
                    currentTime = Integer.parseInt(event[0]);
                }
                catch (Exception e) {
                    sc.close();
                    throw new IllegalArgumentException("Invalid event format in line " + currentLine);
                }
                // cria um novo balcao e processa todos os balcoes ate ao tempo atual
                counters = Arrays.copyOf(counters, counters.length+1); // extende a array
                counters[counters.length-1] = new Counter(counters.length-1, currentTime, this.sb);
                processAllCountersUntil(currentTime);
            }
            else { // se o evento nao for de nenhum tipo (invalido)
                sc.close();
                throw new IllegalArgumentException("Invalid event format in line " + currentLine);
            }
        }
        
        sb.append("All clients have been assigned to a counter!");
        sb.append(EOL);
        
        // processa todos os balcoes ate que todos os clientes tenham sido processados
        while (!isProcessingFinished()) {
            processAllCountersFor(counters[firstCounterToFinishClient()].currentClient().getClientProcessingDuration());
        }
        
        sb.append("All clients have been processed!");
        sb.append(EOL);
        
        // calcula o total de vendas
        for (Counter c : counters) {
            totalSalesAmount += c.getSalesAmount();
        }

        sb.append(String.format(Locale.US,"Total sales: %.2f€.", totalSalesAmount));
        
        sc.close();
    }
   
    /** 
     * Processa todos os balcoes até um determinado momento no tempo.
     * 
     * @param untilTime tempo até ao qual se pretende processar os balcoes
     */
    private void processAllCountersUntil(int untilTime) {
        for (Counter c : counters) {
            c.processQueueUntilTime(untilTime);
        }
    }
    
    
    /**
     * Processa todos os balcões durante um determinado periodo de tempo.
     * 
     * @param tillTime tempo de processamento
     */
    private void processAllCountersFor(int tillTime) {
        for (Counter c : counters) {
            c.processQueueForDuration(tillTime);
        }
    }
    
    
    /**
     * Processa um cliente, atribuindo-o ao balcão que termina mais cedo.
     * 
     * @param client cliente a processar
     * @return tempo atual após o processamento do cliente
     */
    public int processClient(Client client) {
        int firstCounterToFinish = firstCounterToFinish();
        counters[firstCounterToFinish].addClient(client);
        
        sb.append("[TS " + client.getArrivalTime() + "] ");
        sb.append("Client " + client.getClientCode() + " assigned to counter " + firstCounterToFinish);
        sb.append(", processing will take " + client.getClientProcessingDuration() + ".");
        sb.append(EOL);
        
        return counters[firstCounterToFinish].getCurrentTime();
    }
    
    
    /**
     * Devolve o id do balcão que termina mais cedo de processar todos os seus clientes.
     * 
     * @return o id do balcão que termina mais cedo
     */
    public int firstCounterToFinish() {
        int first = 0;
        for (int i = 1; i < counters.length; i++) {
            // tempo do primeiro balcao + tempo total de processamento
            int firstTime = counters[first].getCurrentTime() + counters[first].getTotalProcessingDuration();
            // tempo do balcao i + tempo total de processamento
            int iTime = counters[i].getCurrentTime() + counters[i].getTotalProcessingDuration();
            // se o tempo do balcao i for menor que o tempo do balcao first
            if (iTime < firstTime) {
                first = i;
            }
        }
        return first;
    }
    
    
    /**
     * Devolve o id do balcão que termina mais cedo de processar o seu cliente atual.
     * 
     * @return o id do balcão que termina mais cedo de processar o seu cliente atual
     */
    public int firstCounterToFinishClient() {
        int first = 0;
        int firstTime = 0;
        int iTime = 0;
        for (int i = 1; i < counters.length; i++) {
            // se o balcao first nao estiver vazio
            if (!counters[first].isEmpty()) {
                // tempo do balcao first + tempo de processamento do cliente
                firstTime = counters[first].getCurrentTime() + counters[first].currentClient().getClientProcessingDuration();
            }
            // se estiver
            else {
                // escolhe o balcao i como o primeiro
                first = i;
                continue;
            }
            // se o balcao i nao estiver vazio
            if (!counters[i].isEmpty()) {
                // tempo do balcao i + tempo de processamento do cliente
                iTime = counters[i].getCurrentTime() + counters[i].currentClient().getClientProcessingDuration();
            }
            // se estiver
            else {
                // continua porque o first ja esta definido como first
                continue;
            }
            // se existir clientes em ambos os balcoes
            // e o tempo do balcao i for menor que o tempo do balcao first
            if (iTime < firstTime) {
                // balcao i passa a ser o first
                first = i;
            }
        }
        return first;
    }
    
    
    /**
     * Verifica se todos os clientes foram processados.
     * 
     * @return true se todos os clientes foram processados, false caso contrário
     */
    public boolean isProcessingFinished() {
        for (Counter c : counters) {
            if (!c.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    
    /** 
     * Devolve o total de vendas.
     * 
     * @return o total de vendas
     */
    public double getTotalSalesAmount() {
        return totalSalesAmount;
    }
}
