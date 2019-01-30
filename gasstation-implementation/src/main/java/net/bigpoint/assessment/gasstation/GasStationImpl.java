package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GasStationImpl implements GasStation {

    private Collection<GasPump> gasPumps = new ArrayList<>();
    private Map<GasType, Double> gasPrices = new ConcurrentHashMap<>();

    private double revenue = 0;
    private int numberOfSales = 0;
    private int numberOfCancellationsNoGas = 0;
    private int numberOfCancellationsTooExpensive = 0;

    public void addGasPump(GasPump pump) {
        synchronized (gasPumps) {
            gasPumps.add(pump);
        }
    }

    public Collection<GasPump> getGasPumps() {
        return new ArrayList<>(gasPumps);
    }

    public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException, GasTooExpensiveException {
        return 0;
    }

    public double getRevenue() {
        return revenue;
    }

    public int getNumberOfSales() {
        return numberOfSales;
    }

    public int getNumberOfCancellationsNoGas() {
        return numberOfCancellationsNoGas;
    }

    public int getNumberOfCancellationsTooExpensive() {
        return numberOfCancellationsTooExpensive;
    }

    public double getPrice(GasType type) {
        return gasPrices.get(type);
    }

    public void setPrice(GasType type, double price) {
        gasPrices.put(type, price);
    }
}
