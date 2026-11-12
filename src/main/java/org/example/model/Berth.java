package org.example.model;

import java.util.Objects;

/**
 * Representa um Berço de Atracação comercial do Porto de Santos (RF005 e RF006).
 */
public class Berth {
    private String code;
    private String name;
    private String sector;
    private double maxDraft;
    private CargoCategory cargoCategory;
    private double nominalDailyRateTons;
    private double lengthMeters;

    public Berth(String code, String name, String sector, double maxDraft,
                 CargoCategory cargoCategory, double nominalDailyRateTons, double lengthMeters) {
        setCode(code);
        setName(name);
        setSector(sector);
        setMaxDraft(maxDraft);
        setCargoCategory(cargoCategory);
        setNominalDailyRateTons(nominalDailyRateTons);
        setLengthMeters(lengthMeters);
    }

    public Berth(String code, String name, String sector, double maxDraft, CargoCategory cargoCategory) {
        this(code, name, sector, maxDraft, cargoCategory, 15000.0, 280.0);
    }

    /**
     * RN004 — Cálculo Determinístico da Duração de Atracação:
     * D_dias = ceil(Tonelagem / Taxa Nominal Diária do Berço)
     */
    public int calculateStayDurationDays(double volumeTons) {
        if (volumeTons <= 0 || nominalDailyRateTons <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(volumeTons / nominalDailyRateTons));
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O código do berço é obrigatório.");
        }
        this.code = code.trim().toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome/terminal do berço é obrigatório.");
        }
        this.name = name.trim();
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        if (sector == null || sector.isBlank()) {
            throw new IllegalArgumentException("A região/setor do berço é obrigatória.");
        }
        this.sector = sector.trim();
    }

    public double getMaxDraft() {
        return maxDraft;
    }

    public void setMaxDraft(double maxDraft) {
        if (maxDraft <= 0 || maxDraft > 25.0) {
            throw new IllegalArgumentException("O calado máximo deve estar entre 0,1m e 25,0m.");
        }
        this.maxDraft = maxDraft;
    }

    public CargoCategory getCargoCategory() {
        return cargoCategory;
    }

    public void setCargoCategory(CargoCategory cargoCategory) {
        this.cargoCategory = Objects.requireNonNull(cargoCategory, "O perfil de carga é obrigatório.");
    }

    public double getNominalDailyRateTons() {
        return nominalDailyRateTons;
    }

    public void setNominalDailyRateTons(double nominalDailyRateTons) {
        if (nominalDailyRateTons <= 0) {
            throw new IllegalArgumentException("A taxa nominal diária deve ser positiva.");
        }
        this.nominalDailyRateTons = nominalDailyRateTons;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }

    public void setLengthMeters(double lengthMeters) {
        if (lengthMeters <= 0) {
            throw new IllegalArgumentException("O comprimento do berço deve ser positivo.");
        }
        this.lengthMeters = lengthMeters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Berth berth)) return false;
        return Objects.equals(code, berth.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return String.format("%s — %s (%s | Calado Máx: %.1fm)", code, name, cargoCategory.getDisplayName(), maxDraft);
    }
}
