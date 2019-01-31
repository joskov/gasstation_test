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

    private List<Thread> waitingQueue = new ArrayList<>();

    private Object gasPumpSearchLock = new Object();

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


        GasPump pump;
        while (true) {
            pump = getAvailablePump(type, amountInLiters);
            if (pump != null) {
                break;
            }

            // add the thread to the waiting queue
            synchronized (waitingQueue) {
                waitingQueue.add(Thread.currentThread());
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // interrupted
            }
        }

        pump.pumpGas(amountInLiters);
        // add back the pump to the available ones
        synchronized (availablePumps) {
            availablePumps.add(pump);
        }
        // interrupt the waiting queue
        synchronized (waitingQueue) {
            List<Thread> oldWaitingQueue = new ArrayList<>(waitingQueue);
            waitingQueue.clear();
            oldWaitingQueue.forEach(thread -> thread.interrupt());
        }

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

    /**
     * Gets a free gas pump with enough gas
     * @param type
     * @param amountInLiters
     * @return available pump or null if there is no pump currently available
     * @throws NotEnoughGasException
     *              Thrown if there is no pump with enough gas
     */
    private GasPump getAvailablePump(GasType type, double amountInLiters) throws NotEnoughGasException {
        synchronized (gasPumpSearchLock) {
            // System.out.println(availablePumps);
            List<GasPump> filteredPumps = gasPumps.stream()
                    .filter(gasPump -> gasPump.getGasType() == type)
                    .filter(gasPump -> gasPump.getRemainingAmount() >= amountInLiters)
                    .collect(Collectors.toList());
            if (filteredPumps.isEmpty()) {
                // TODO: move this exception to the buyGas method
                numberOfCancellationsNoGas.incrementAndGet();
                throw new NotEnoughGasException();
            }

            List<GasPump> availableFilteredPumps;
            availableFilteredPumps = filteredPumps.stream().filter(gasPump -> availablePumps.contains(gasPump)).collect(Collectors.toList());
            if (availableFilteredPumps.isEmpty()) {
                return null;
            } else {
                GasPump result = availableFilteredPumps.get(0);
                synchronized (availablePumps) {
                    availablePumps.remove(result);
                }
                return result;
            }
        }
    }
}
