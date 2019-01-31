package net.bigpoint.assessment.gasstation;

import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

public class GasStationImpl implements GasStation {

    private Collection<GasPump> gasPumps = new ArrayList<>();
    private Map<GasType, Double> gasPrices = new ConcurrentHashMap<>();
    private Collection<GasPump> availablePumps = new ArrayList<>();

    private DoubleAdder revenue = new DoubleAdder();
    private AtomicInteger numberOfSales = new AtomicInteger();
    private AtomicInteger numberOfCancellationsNoGas = new AtomicInteger();
    private AtomicInteger numberOfCancellationsTooExpensive = new AtomicInteger();

    public void addGasPump(GasPump pump) {
        synchronized (gasPumps) {
            gasPumps.add(pump);
            availablePumps.add(pump);
        }
    }

    public Collection<GasPump> getGasPumps() {
        return new ArrayList<>(gasPumps);
    }

    public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException, GasTooExpensiveException {
        Double price = gasPrices.get(type);
        if (price == null || price > maxPricePerLiter) {
            numberOfCancellationsTooExpensive.incrementAndGet();
            throw new GasTooExpensiveException();
        }

        List<GasPump> filteredPumps = gasPumps.stream()
                .filter(gasPump -> gasPump.getGasType() == type)
                .filter(gasPump -> gasPump.getRemainingAmount() >= amountInLiters)
                .collect(Collectors.toList());
        if (filteredPumps.isEmpty()) {
            numberOfCancellationsNoGas.incrementAndGet();
            throw new NotEnoughGasException();
        }

        List<GasPump> availableFilteredPumps;
        GasPump pump;
        while (true) {
            availableFilteredPumps = filteredPumps.stream().filter(gasPump -> availablePumps.contains(gasPump)).collect(Collectors.toList());
            if (availableFilteredPumps.isEmpty()) {
                // TODO: synchronize
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // interrupted
                }
                continue;
            }
            synchronized (availablePumps) {
                pump = availableFilteredPumps.get(0);
                availablePumps.remove(pump);
            }
            break;
        }

        pump.pumpGas(amountInLiters);
        availablePumps.add(pump);
        // TODO: unlock the pump

        double priceToPay = amountInLiters * price;
        numberOfSales.incrementAndGet();
        revenue.add(priceToPay);

        return priceToPay;
    }

    public double getRevenue() {
        return revenue.sum();
    }

    public int getNumberOfSales() {
        return numberOfSales.intValue();
    }

    public int getNumberOfCancellationsNoGas() {
        return numberOfCancellationsNoGas.intValue();
    }

    public int getNumberOfCancellationsTooExpensive() {
        return numberOfCancellationsTooExpensive.intValue();
    }

    public double getPrice(GasType type) {
        return gasPrices.get(type);
    }

    public void setPrice(GasType type, double price) {
        gasPrices.put(type, price);
    }
}
