package org.example.model;

import java.util.Objects;

/**
 * Representa uma escala/atracação programada no Lineup do Porto de Santos (RF011 e RF012).
 * Implementa verificações de sobreposição temporal (RN001) e cores normatizadas (RN005).
 */
public class ShipSchedule {
    public static final String STATUS_OPERANDO = "Operando / Prioridade";
    public static final String STATUS_PROGRAMADO = "Programado";
    public static final String STATUS_CONFIRMADO = "Confirmado";

    private String id;
    private Berth berth;
    private RegisteredVessel vessel;
    private int startDay;
    private int endDay;
    private double cargoVolumeTons;
    private String operationalStatus;
    private String cargoCommodity;
    private String agencyName;

    public ShipSchedule(String id, Berth berth, RegisteredVessel vessel,
                        int startDay, int endDay, double cargoVolumeTons,
                        String operationalStatus, String cargoCommodity, String agencyName) {
        setId(id);
        setBerth(berth);
        setVessel(vessel);
        setDays(startDay, endDay);
        setCargoVolumeTons(cargoVolumeTons);
        setOperationalStatus(operationalStatus);
        setCargoCommodity(cargoCommodity);
        setAgencyName(agencyName);
    }

    /**
     * RN001 — Anti-Sobreposição Temporal em Berços Acostáveis:
     * Para o Navio A com intervalo [I_A, F_A] e Navio B com [I_B, F_B],
     * NÃO há colisão somente se: (F_A <= I_B) v (F_B <= I_A).
     */
    public boolean overlapsWith(int otherStartDay, int otherEndDay) {
        return !((this.endDay <= otherStartDay) || (otherEndDay <= this.startDay));
    }

    public boolean overlapsWith(ShipSchedule other) {
        if (other == null) return false;
        if (!Objects.equals(this.berth.getCode(), other.berth.getCode())) {
            return false;
        }
        return overlapsWith(other.startDay, other.endDay);
    }

    /**
     * RN005 — Cores Normatizadas de Status no Lineup:
     * #e74c3c (Vermelho): Operando / Prioridade
     * #e67e22 (Laranja): Programado
     * #2ecc71 (Verde): Confirmado
     */
    public String getStatusHexColor() {
        if (operationalStatus == null) return "#e67e22";
        String normalized = operationalStatus.toLowerCase();
        if (normalized.contains("operando") || normalized.contains("prioridade")) {
            return "#e74c3c";
        } else if (normalized.contains("confirmado")) {
            return "#2ecc71";
        } else {
            return "#e67e22";
        }
    }

    public int getDurationDays() {
        return Math.max(1, endDay - startDay);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O ID da escala é obrigatório.");
        }
        this.id = id.trim();
    }

    public Berth getBerth() {
        return berth;
    }

    public void setBerth(Berth berth) {
        this.berth = Objects.requireNonNull(berth, "O berço de atracação é obrigatório.");
    }

    public RegisteredVessel getVessel() {
        return vessel;
    }

    public void setVessel(RegisteredVessel vessel) {
        this.vessel = Objects.requireNonNull(vessel, "O navio é obrigatório.");
    }

    public int getStartDay() {
        return startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setDays(int startDay, int endDay) {
        if (startDay < 1 || startDay > 30) {
            throw new IllegalArgumentException("O dia de início deve estar entre 1 e 30.");
        }
        if (endDay < 1 || endDay > 30) {
            throw new IllegalArgumentException("O dia de término deve estar entre 1 e 30.");
        }
        if (endDay <= startDay) {
            throw new IllegalArgumentException("O Dia Fim deve ser estritamente posterior ao Dia Início (F > I).");
        }
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public double getCargoVolumeTons() {
        return cargoVolumeTons;
    }

    public void setCargoVolumeTons(double cargoVolumeTons) {
        if (cargoVolumeTons <= 0) {
            throw new IllegalArgumentException("O volume em toneladas deve ser maior que zero.");
        }
        this.cargoVolumeTons = cargoVolumeTons;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        if (operationalStatus == null || operationalStatus.isBlank()) {
            this.operationalStatus = STATUS_PROGRAMADO;
        } else {
            this.operationalStatus = operationalStatus.trim();
        }
    }

    public String getCargoCommodity() {
        return cargoCommodity;
    }

    public void setCargoCommodity(String cargoCommodity) {
        this.cargoCommodity = (cargoCommodity == null || cargoCommodity.isBlank())
                ? vessel.getSupportedCargoCategory().getDisplayName()
                : cargoCommodity.trim();
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = (agencyName == null || agencyName.isBlank()) ? "Santos Maritime Agency" : agencyName.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipSchedule that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
