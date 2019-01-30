package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GasStationImpl implements GasStation {

    private Collection<GasPump> GasPumps = new ArrayList<>();
    private Map<GasType, Double> GasPrices = new ConcurrentHashMap<>();

    private double Revenue = 0;
    private int NumberOfSales = 0;
    private int NumberOfCancellationsNoGas = 0;
    private int NumberOfCancellationsTooExpensive = 0;

    public void addGasPump(GasPump pump) {
        synchronized (GasPumps) {
            GasPumps.add(pump);
        }
    }

    public Collection<GasPump> getGasPumps() {
        return new ArrayList<>(GasPumps);
    }

    public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException, GasTooExpensiveException {
        return 0;
    }

    public double getRevenue() {
        return Revenue;
    }

    public int getNumberOfSales() {
        return NumberOfSales;
    }

    public int getNumberOfCancellationsNoGas() {
        return NumberOfCancellationsNoGas;
    }

    public int getNumberOfCancellationsTooExpensive() {
        return NumberOfCancellationsTooExpensive;
    }

    public double getPrice(GasType type) {
        return GasPrices.get(type);
    }

    public void setPrice(GasType type, double price) {
        GasPrices.put(type, price);
    }
}
