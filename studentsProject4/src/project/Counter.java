package project;

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
	}
	
	public void addClient(Client client) {
	    totalProcessingDuration += client.getClientProcessingDuration();
	    clientQueue.enqueue(client);
	}
	
	public Client currentClient() {
		return clientQueue.isEmpty() ? null : clientQueue.front();
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
	        salesAmount += getSalesAmount(currentClient().getShoppingCart());
	        currentTime += currentClient().getClientProcessingDuration();
	        removeClient();
	    }
	    currentTime += duration;
	    // se ja nao houver tempo suf para processar o cliente,
	    // retira-se o tempo restante ao tempo do cliente
	    if (!isEmpty()) {
	        // TODO VER ISTO QUE TA MAL
	        totalProcessingDuration -= duration;
	        currentClient().setClientProcessingDuration(currentClient().getClientProcessingDuration() - duration);
	    }
	}
	
	public void processQueueUntilTime(int time) {
        while (!isEmpty() && time >= currentClient().getClientProcessingDuration()) {
            time -= currentClient().getClientProcessingDuration();
            salesAmount += getSalesAmount(currentClient().getShoppingCart());
            currentTime += currentClient().getClientProcessingDuration();
            removeClient();
        }
        currentTime += time;
        if (!isEmpty()) {
            currentClient().setClientProcessingDuration(currentClient().getClientProcessingDuration() - time);
        }
	}
	
	private double getSalesAmount(Product[] shoppingCart) {
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
