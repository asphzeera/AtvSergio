package org.example.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Representa uma embarcação mercante cadastrada na frota (RF008 e RF009).
 * Contém validação rigorosa de IMO (RN007).
 */
public class RegisteredVessel {
    public static final Pattern IMO_PATTERN = Pattern.compile("^IMO-\\d{7}$|^IMO-\\d+$");

    private String imoCode;
    private String name;
    private String vesselClass; // Panamax, Capesize, Post-Panamax, Handymax, Suezmax, etc.
    private double maxOperationalDraft;
    private double deadweightTonnage; // DWT (Porte Bruto)
    private CargoCategory supportedCargoCategory;
    private String flagCountry;

    public RegisteredVessel(String imoCode, String name, String vesselClass,
                            double maxOperationalDraft, double deadweightTonnage,
                            CargoCategory supportedCargoCategory, String flagCountry) {
        setImoCode(imoCode);
        setName(name);
        setVesselClass(vesselClass);
        setMaxOperationalDraft(maxOperationalDraft);
        setDeadweightTonnage(deadweightTonnage);
        setSupportedCargoCategory(supportedCargoCategory);
        setFlagCountry(flagCountry);
    }

    /**
     * RN007 — Validação de IMO Único e Internacional:
     * Código deve obrigatoriamente iniciar com "IMO-" seguido de dígitos numéricos válidos.
     */
    public static boolean isValidImo(String imo) {
        if (imo == null) return false;
        return IMO_PATTERN.matcher(imo.trim()).matches();
    }

    public String getImoCode() {
        return imoCode;
    }

    public void setImoCode(String imoCode) {
        if (!isValidImo(imoCode)) {
            throw new IllegalArgumentException("RN007: O código IMO deve iniciar obrigatoriamente com 'IMO-' seguido apenas por dígitos numéricos (ex: IMO-9456123).");
        }
        this.imoCode = imoCode.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do navio é obrigatório.");
        }
        this.name = name.trim();
    }

    public String getVesselClass() {
        return vesselClass;
    }

    public void setVesselClass(String vesselClass) {
        if (vesselClass == null || vesselClass.isBlank()) {
            throw new IllegalArgumentException("A classe do navio é obrigatória.");
        }
        this.vesselClass = vesselClass.trim();
    }

    public double getMaxOperationalDraft() {
        return maxOperationalDraft;
    }

    public void setMaxOperationalDraft(double maxOperationalDraft) {
        if (maxOperationalDraft <= 0 || maxOperationalDraft > 25.0) {
            throw new IllegalArgumentException("O calado operacional máximo deve estar entre 0,1m e 25,0m.");
        }
        this.maxOperationalDraft = maxOperationalDraft;
    }

    public double getDeadweightTonnage() {
        return deadweightTonnage;
    }

    public void setDeadweightTonnage(double deadweightTonnage) {
        if (deadweightTonnage <= 0) {
            throw new IllegalArgumentException("O Porte Bruto (DWT) deve ser maior que zero.");
        }
        this.deadweightTonnage = deadweightTonnage;
    }

    public CargoCategory getSupportedCargoCategory() {
        return supportedCargoCategory;
    }

    public void setSupportedCargoCategory(CargoCategory supportedCargoCategory) {
        this.supportedCargoCategory = Objects.requireNonNull(supportedCargoCategory, "O perfil de carga do navio é obrigatório.");
    }

    public String getFlagCountry() {
        return flagCountry;
    }

    public void setFlagCountry(String flagCountry) {
        this.flagCountry = (flagCountry == null || flagCountry.isBlank()) ? "Panamá" : flagCountry.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisteredVessel that)) return false;
        return Objects.equals(imoCode, that.imoCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imoCode);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) — Calado %.1fm | %s", name, imoCode, maxOperationalDraft, supportedCargoCategory.getDisplayName());
    }
}
