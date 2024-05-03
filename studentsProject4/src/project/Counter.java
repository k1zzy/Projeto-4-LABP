package project;

/**
 * Classe que representa um Counter de uma loja.
 * Um Counter é um balcão de atendimento que processa clientes.
 * Cada balcão tem um id, um tempo atual, um montante total de vendas, um tempo total de processamento,
 * 
 * @author Rodrigo Afonso (61839)
 * @version 1.0
 */

import java.util.Locale;

public class Counter {
	private final int counterId;
	private int currentTime;
	private double salesAmount;
	private int totalProcessingDuration;
	private Queue<Client> clientQueue;
	private StringBuilder log;
	public static final String EOL = System.lineSeparator();
	
	/** 
	 * Contrutor da classe Counter que representa um balcão que recebe
	 * um id, o tempo atual e um StringBuilder para o log.
	 * 
	 * @param counterId id do balcão
	 * @param currentTime tempo atual
	 * @param sb StringBuilder para o log
	 */
	public Counter(int counterId, int currentTime, StringBuilder sb) {
		this.counterId = counterId;
		this.currentTime = currentTime;
		this.clientQueue = new LinkedQueue<>();
		this.salesAmount = 0;
		this.totalProcessingDuration = 0;
		this.log = sb;
		
		log.append("[TS " + currentTime + "] ");
        log.append("Counter " + counterId + " open.");
        log.append(EOL);
	}
	
	
	/** 
	 * Adiciona um cliente à frente da fila do balcão.
	 * Adiciona o tempo de processamento do cliente ao tempo total de processamento do balcão.
	 * Processa a fila de clientes até ao tempo de chegada do cliente.
	 * 
	 * @param client cliente a adicionar
	 */
	public void addClient(Client client) {
	    totalProcessingDuration += client.getClientProcessingDuration();
	    processQueueUntilTime(client.getArrivalTime());
	    clientQueue.enqueue(client);
	}
	
	/**
	 * Devolve o cliente que está à frente na fila (que está a ser processado).
	 * 
	 * @requires A fila de clientes não estar vazia
	 * @return o cliente que está à frente na fila
	 */
	public Client currentClient() {
		return clientQueue.front();
	}
	
	/**
	 * Remove o cliente que está à frente na fila (se já foi processado totalmente).
	 * Retira do tempo total de processamento do balcão o tempo de processamento do cliente.
	 */
	public void removeClient() {
	    totalProcessingDuration -= currentClient().getClientProcessingDuration();
		clientQueue.dequeue();
	}
	
	
	/** 
	 * Verifica se a fila de clientes está vazia.
	 * 
	 * @return boolean true se a fila estiver vazia, false caso contrário
	 */
	public boolean isEmpty() {
	    return clientQueue.isEmpty();
	}
	
	
	/** 
	 * Processa a fila de clientes durante um determinado tempo.
	 * Caso o tempo de processamento do cliente seja menor ou igual ao tempo de processamento,
	 * o cliente é processado e removido da fila.
	 * 
	 * @param duration tempo de processamento
	 */
	public void processQueueForDuration(int duration) {
	    // enquanto existir cliente e enquanto for possivel processa-lo
	    while (!isEmpty() && duration >= currentClient().getClientProcessingDuration()) {
	        duration -= currentClient().getClientProcessingDuration();
	        salesAmount += getClientSalesAmount(currentClient().getShoppingCart());
	        currentTime += currentClient().getClientProcessingDuration();
	        log.append("[TS " + currentTime + "] ");
	        log.append("Client " + currentClient().getClientCode() + " has finished processing. ");
	        log.append("Total wait time: " + (currentTime - currentClient().getArrivalTime()) + ". ");
	        log.append(String.format(Locale.US,"Payment: %.2f€.", getClientSalesAmount(currentClient().getShoppingCart())));
	        log.append(EOL);
	        removeClient();
	    }
	    currentTime += duration;
	    // se ja nao houver tempo suf para processar o cliente,
	    // retira-se a duration ao tempo total de processamento e ao tempo de processamento do cliente
	    if (!isEmpty()) {
	        totalProcessingDuration -= duration;
	        currentClient().setClientProcessingDuration(currentClient().getClientProcessingDuration() - duration);
	    }
	}
	
	
	/** 
	 * Processa a fila de clientes até um determinado momento no tempo.
	 * Caso o tempo de processamento do cliente seja menor ou igual ao tempo de processamento,
	 * o cliente é processado e removido da fila.
	 *
	 * @param time tempo até ao qual se pretende processar a fila
	 */
	public void processQueueUntilTime(int time) {
		// funciona exatamente igual ao processQueueForDuration
		// assim que ao time se subtrai o currentTime
	    time -= currentTime;
        while (!isEmpty() && time >= currentClient().getClientProcessingDuration()) {
            time -= currentClient().getClientProcessingDuration();
            salesAmount += getClientSalesAmount(currentClient().getShoppingCart());
            currentTime += currentClient().getClientProcessingDuration();
            log.append("[TS " + currentTime + "] ");
            log.append("Client " + currentClient().getClientCode() + " has finished processing. ");
            log.append("Total wait time: " + (currentTime - currentClient().getArrivalTime()) + ". ");
            log.append(String.format(Locale.US,"Payment: %.2f€.", getClientSalesAmount(currentClient().getShoppingCart())));
            log.append(EOL);
            removeClient();
        }
        currentTime += time;
        if (!isEmpty()) {
            totalProcessingDuration -= time;
            currentClient().setClientProcessingDuration(currentClient().getClientProcessingDuration() - time);
        }
	}
	
	
	/** 
	 * Devolve o preço total de compras num dado carrinho de compras. 
	 * 
	 * @param shoppingCart array de produtos
	 * @return total de compras do cliente
	 */
	private static double getClientSalesAmount(Product[] shoppingCart) {
        double total = 0;
        for (Product p : shoppingCart) {
            total += p.getProdPrice();
        }
        return total;
    }
	
	
	/** 
	 * Devolve o id do balcão.
	 * 
	 * @return id do balcão
	 */
	public int getCounterId() {
	    return counterId;
	}
	
	
	/** 
	 * Devolve o tempo atual.
	 * 
	 * @return tempo atual
	 */
	public int getCurrentTime() {
	    return currentTime;
	}
	
	
	/**
	 * Devolve o montante total de vendas. 
	 * 
	 * @return o montante total de vendas
	 */
	public double getSalesAmount() {
	    return salesAmount;
	}
	
	
	/** 
	 * Devolve o tempo total de processamento do balcão.
	 * 
	 * @return tempo total de processamento do balcão
	 */
	public int getTotalProcessingDuration() {
	    return totalProcessingDuration;
	}
}
