package org.example.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Agregado da Tábua de Marés Astronômicas de um mês de referência (RF010).
 * Contém 1.440 leituras (30 dias x 48 medições de 30 minutos).
 */
public class TideTable {
    private final String referenceMonth;
    private final List<TideMeasurement> measurements;
    private final Set<Integer> criticalLowTideDays;

    public TideTable(String referenceMonth, List<TideMeasurement> measurements) {
        this.referenceMonth = referenceMonth;
        this.measurements = List.copyOf(measurements);
        this.criticalLowTideDays = measurements.stream()
                .filter(TideMeasurement::isCriticalLowTide)
                .map(TideMeasurement::getDayOfMonth)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public String getReferenceMonth() {
        return referenceMonth;
    }

    public List<TideMeasurement> getMeasurements() {
        return Collections.unmodifiableList(measurements);
    }

    public Set<Integer> getCriticalLowTideDays() {
        return Collections.unmodifiableSet(criticalLowTideDays);
    }

    public boolean isCriticalLowTideDay(int day) {
        return criticalLowTideDays.contains(day);
    }

    public double getMinTideForDay(int day) {
        return measurements.stream()
                .filter(m -> m.getDayOfMonth() == day)
                .mapToDouble(TideMeasurement::getHeightMeters)
                .min()
                .orElse(1.0);
    }

    public double getMaxTideForDay(int day) {
        return measurements.stream()
                .filter(m -> m.getDayOfMonth() == day)
                .mapToDouble(TideMeasurement::getHeightMeters)
                .max()
                .orElse(1.5);
    }
}
