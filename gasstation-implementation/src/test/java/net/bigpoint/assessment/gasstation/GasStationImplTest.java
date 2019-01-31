package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}