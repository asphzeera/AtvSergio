package org.example.model;

/**
 * Representa uma leitura individual de maré astronômica a cada 30 minutos (RF010).
 */
public class TideMeasurement {
    public static final double CRITICAL_LOW_TIDE_LIMIT = 0.4;

    private final int sequenceNumber;
    private final int dayOfMonth;
    private final String monthYear;
    private final String timeFormatted; // ex: "00:00", "00:30", ..., "23:30"
    private final double heightMeters;
    private final String tideState; // "Baixa Crítica (<= 0,4m)", "Maré Vazante", "Maré Enchente", "Preamar"

    public TideMeasurement(int sequenceNumber, int dayOfMonth, String monthYear,
                           String timeFormatted, double heightMeters, String tideState) {
        this.sequenceNumber = sequenceNumber;
        this.dayOfMonth = dayOfMonth;
        this.monthYear = monthYear;
        this.timeFormatted = timeFormatted;
        this.heightMeters = heightMeters;
        this.tideState = tideState;
    }

    public boolean isCriticalLowTide() {
        return heightMeters <= CRITICAL_LOW_TIDE_LIMIT;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public double getHeightMeters() {
        return heightMeters;
    }

    public String getTideState() {
        return tideState;
    }

    public String getHydrodynamicAlert() {
        return isCriticalLowTide()
                ? "BLOQUEIO CALADO > 13,5m (RN002)"
                : "Navegação Normal";
    }
}
