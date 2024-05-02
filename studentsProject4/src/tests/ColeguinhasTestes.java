package tests;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;

import org.junit.Test;

import project.*;

public class ColeguinhasTestes {
    
    @Test
    public void testeFddMelodiasBowser() throws FileNotFoundException {
        // System.out.println("teste");
        StringBuilder sb = new StringBuilder();
        Store store = new Store("./src/tests/storeTest2.txt", sb);
        Product[] shoppingCart1 = { new Product("TEST1", 1, 1) };
        Client client1 = new Client(1, shoppingCart1, 0, 1);
        Product[] shoppingCart2 = { new Product("TEST5", 5, 5) };
        Client client2 = new Client(1, shoppingCart2, 0, 5);
        store.processClient(client2); // 0 -> 5
        store.processClient(client2); // 1 -> 5
        store.processClient(client1); // 1 -> 6
        store.processClient(client1); // 0 -> 6
        int expected = 0;
        int actual = store.firstCounterToFinishClient();
        // System.out.println("acabou");
        assertEquals(expected, actual);
    }
}