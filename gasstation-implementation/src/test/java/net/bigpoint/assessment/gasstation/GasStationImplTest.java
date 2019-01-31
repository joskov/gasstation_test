package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        assertEquals(6, gasStation.getNumberOfSales());
        assertEquals(300, gasStation.getRevenue());

        System.out.printf("Execution time %.2fs.%n", (System.currentTimeMillis() - startTime) / 1000.f);
    }

    @Test
    @Disabled("Performance / Smoke Test")
    void testMultiThreadTest() {
        long startTime = System.currentTimeMillis();

        gasStation.addGasPump(new GasPump(GasType.REGULAR, 250));
        gasStation.addGasPump(new GasPump(GasType.REGULAR, 250));
        gasStation.addGasPump(new GasPump(GasType.REGULAR, 250));
        gasStation.addGasPump(new GasPump(GasType.DIESEL, 250));
        gasStation.addGasPump(new GasPump(GasType.DIESEL, 250));
        gasStation.addGasPump(new GasPump(GasType.SUPER, 250));

        Map<GasType, Integer> cars = new HashMap<>();
        cars.put(GasType.REGULAR, 8);
        cars.put(GasType.DIESEL, 3);
        cars.put(GasType.SUPER, 7);

        List<Thread> threads = new ArrayList<>();
        cars.forEach((gasType, v) -> {
            gasStation.setPrice(gasType, 5);
            for (int i = 0; i < v; i++) {
                Thread thread = new Thread(() -> {
                    try {
                        gasStation.buyGas(gasType, 10, 10);
                    } catch (NotEnoughGasException e) {
                        e.printStackTrace();
                    } catch (GasTooExpensiveException e) {
                        e.printStackTrace();
                    }
                });
                threads.add(thread);
            }
        });

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