package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Store {
    private Counter[] counters;
    private final Product[] productCatalog;
    private double totalSalesAmount;
    private StringBuilder sb;
    public static final String EOL = System.lineSeparator();
    
    public Store(String fileName, StringBuilder sb) {
        this.sb = sb;
        String[] catalog = readStore(fileName); // le o ficheiro e guarda o conteudo num array
        this.counters = new Counter[Integer.parseInt(catalog[0])]; // cria os balcoes com o tamanho indicado
        this.productCatalog = new Product[Integer.parseInt(catalog[1])]; // cria o catalogo de produtos com o tamanho indicado
        fillProducts(catalog); // preenche o catalogo com os produtos do ficheiro
        iniciateCounters(); // inicia os balcoes
    }
    
    private static String[] readStore(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            Scanner sc = new Scanner(new File(fileName));
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                sb.append(" ");
            }
            sc.close();
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace(); //TODO esta estranho
        }
        return sb.toString().split(" ");
    }
    
    private void fillProducts(String[] catalog) {
        try {
            for (int i = 0; i < productCatalog.length; i++) {
                // i*2+2+i (funciona cheguei la por tentativa e erro)
                productCatalog[i] = new Product(catalog[i*2+2+i], Double.parseDouble(catalog[i*2+3+i]), Integer.parseInt(catalog[i*2+4+i]));
            }
        }
        // se tiver elementos a mais ou a menos
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid catalog format");
        }
    }
    
    private void iniciateCounters() {
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new Counter(i, 0, this.sb);
        }
    }
    
    public void processEvents(String fileName) throws FileNotFoundException {
        int currentTime = 0;
        int currentUserId = 0;
        int currentLine = 0;
        int clientProcessingDuration = 0;
        Scanner sc = null;
        try {
            sc = new Scanner(new File(fileName));
        }
        catch (FileNotFoundException e) {
            sc.close();
            throw new FileNotFoundException("Events file does not exist");
        }
        
        while(sc.hasNextLine()) {
            currentLine++;
            String[] event = sc.nextLine().split(" ");
            
            if (event[1].equals("CLIENT")) { // se o evento for do tipo CLIENT
                Product[] cart = new Product[event.length - 3];
                try {
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
        
        while (!areAllCountersDone()) {
            if (!counters[firstCounterToFinishClient()].isEmpty()) {
                processAllCountersFor(counters[firstCounterToFinishClient()].currentClient().getClientProcessingDuration());
            }
            else {
                processAllCountersFor(counters[firstCounterToFinish()].currentClient().getClientProcessingDuration());
            }
        }
        
        for (Counter c : counters) {
            totalSalesAmount += c.getSalesAmount();
        }
        
        sc.close();
    }
    
    private boolean areAllCountersDone() {
        for (Counter c : counters) {
            if (c.getTotalProcessingDuration() > 0) {
                return false;
            }
        }
        return true;
    }
    
    private void processAllCountersUntil(int untilTime) {
        for (Counter c : counters) {
            c.processQueueUntilTime(untilTime);
        }
    }
    
    private void processAllCountersFor(int untilTime) {
        for (Counter c : counters) {
            c.processQueueForDuration(untilTime);
        }
    }
    
    public int processClient(Client client) {
        Counter counter = counters[firstCounterToFinish()]; // TODO SE QUE NAO DA
        counters[firstCounterToFinish()].addClient(client);
        
        sb.append("[TS " + client.getArrivalTime() + "] ");
        sb.append("Client " + client.getClientCode() + " assigned to counter " + counter.getCounterId());
        sb.append(", processing wil take " + counter.getTotalProcessingDuration() + ".");
        sb.append(EOL);
        
        return counter.getCurrentTime();
    }
    
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
    
    public int firstCounterToFinishClient() {
        int first = 0;
        int firstTime = 0;
        int iTime = 0;
        // TODO NAO POSSO METER OS DOISS IFs JUNTOS PQ SENAO HA VEZES QUE NUNCA ENTRA
        for (int i = 0; i < counters.length; i++) {
            if (!counters[i].isEmpty() && !counters[first].isEmpty()) {
                firstTime = counters[first].getCurrentTime() + counters[first].currentClient().getClientProcessingDuration();
                iTime = counters[i].getCurrentTime() + counters[i].currentClient().getClientProcessingDuration();
            }
            if (!counters[i].isEmpty() && !counters[first].isEmpty() && (iTime < firstTime)) {
                first = i;
            }
        }
        return first;
    }
    
    public boolean isProcessingFinished() {
        for (Counter c : counters) {
            if (!c.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public double getTotalSalesAmount() {
        return totalSalesAmount;
    }
}
