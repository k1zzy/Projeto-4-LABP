package project;

import java.util.Locale;

public class Counter {
	private final int counterId;
	private int currentTime;
	private double salesAmount;
	private int totalProcessingDuration;
	private Queue<Client> clientQueue;
	private StringBuilder log;
	public static final String EOL = System.lineSeparator();
	
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
	
	public void addClient(Client client) {
	    totalProcessingDuration += client.getClientProcessingDuration();
	    currentTime = client.getArrivalTime();
	    clientQueue.enqueue(client);
	}
	
	/**
	 * Devolve o cliente que está a ser processado.
	 * 
	 * @requires A fila de clientes não estar vazia
	 * @return o cliente que está à frente na fila
	 */
	public Client currentClient() {
		return clientQueue.front();
	}
	
	public void removeClient() {
	    totalProcessingDuration -= currentClient().getClientProcessingDuration();
		clientQueue.dequeue();
	}
	
	public boolean isEmpty() {
	    return clientQueue.isEmpty();
	}
	
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
	
	public void processQueueUntilTime(int time) {
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
	
	private static double getClientSalesAmount(Product[] shoppingCart) {
        double total = 0;
        for (Product p : shoppingCart) {
            total += p.getProdPrice();
        }
        return total;
    }
	
	public int getCounterId() {
	    return counterId;
	}
	
	public int getCurrentTime() {
	    return currentTime;
	}
	
	public double getSalesAmount() {
	    return salesAmount;
	}
	
	public int getTotalProcessingDuration() {
	    return totalProcessingDuration;
	}
}
