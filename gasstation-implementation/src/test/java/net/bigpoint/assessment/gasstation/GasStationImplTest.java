package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GasStationImplTest {

    private GasStation gasStation;

    @BeforeEach
    void setUp() {
        gasStation = new GasStationImpl();
    }

    @Test
    void testBuyGas() throws NotEnoughGasException, GasTooExpensiveException {
        gasStation.setPrice(GasType.REGULAR, 5);
        gasStation.addGasPump(new GasPump(GasType.REGULAR, 20));

        gasStation.buyGas(GasType.REGULAR, 10, 10);
        assertEquals(1, gasStation.getNumberOfSales());
        assertEquals(10 * 5, gasStation.getRevenue());
    }

    @Test
    void testPriceNotSet() {
        assertThrows(GasTooExpensiveException.class, () ->
                gasStation.buyGas(GasType.REGULAR, 10, 10)
        );
    }

    @Test
    void testNoPump() {
        gasStation.setPrice(GasType.REGULAR, 5);

        assertThrows(NotEnoughGasException.class, () ->
                gasStation.buyGas(GasType.REGULAR, 10, 10)
        );
    }

    @Test
    @Disabled("Performance / Smoke Test")
    void testMultiThreadSingleType() {
        long startTime = System.currentTimeMillis();
        gasStation.setPrice(GasType.REGULAR, 5);
        gasStation.addGasPump(new GasPump(GasType.REGULAR, 250));
        gasStation.addGasPump(new GasPump(GasType.REGULAR, 250));
        gasStation.addGasPump(new GasPump(GasType.REGULAR, 250));

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Thread thread = new Thread(() -> {
                try {
                    gasStation.buyGas(GasType.REGULAR, 10, 10);
                } catch (NotEnoughGasException e) {
                    e.printStackTrace();
                } catch (GasTooExpensiveException e) {
                    e.printStackTrace();
                }
            });
            threads.add(thread);
        }

        // start the threads
        threads.forEach(thread -> thread.start());
        // wait for the threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.printf("Execution time %.2fs.%n", (System.currentTimeMillis() - startTime) / 1000.f);
    }
}