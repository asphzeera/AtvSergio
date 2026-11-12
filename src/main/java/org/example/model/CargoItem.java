package org.example.model;

import java.util.Objects;

/**
 * Representa um item/commodity do Catálogo de Cargas e Perfis Portuários APS (RF007).
 */
public class CargoItem {
    private String code;
    private String commodityName;
    private CargoCategory category;
    private String operationalDirection; // "↓ Descarga", "↑ Carga", "↓↑ Misto"
    private double nominalDailyRateTons; // Taxa diária nominal (t/dia)
    private String unitDescription;

    public CargoItem(String code, String commodityName, CargoCategory category,
                     String operationalDirection, double nominalDailyRateTons, String unitDescription) {
        setCode(code);
        setCommodityName(commodityName);
        setCategory(category);
        setOperationalDirection(operationalDirection);
        setNominalDailyRateTons(nominalDailyRateTons);
        setUnitDescription(unitDescription);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O código da carga não pode ser vazio.");
        }
        this.code = code.trim().toUpperCase();
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        if (commodityName == null || commodityName.isBlank()) {
            throw new IllegalArgumentException("O nome da commodity não pode ser vazio.");
        }
        this.commodityName = commodityName.trim();
    }

    public CargoCategory getCategory() {
        return category;
    }

    public void setCategory(CargoCategory category) {
        this.category = Objects.requireNonNull(category, "A categoria APS é obrigatória.");
    }

    public String getOperationalDirection() {
        return operationalDirection;
    }

    public void setOperationalDirection(String operationalDirection) {
        if (operationalDirection == null || operationalDirection.isBlank()) {
            throw new IllegalArgumentException("O sentido operacional é obrigatório.");
        }
        this.operationalDirection = operationalDirection.trim();
    }

    public double getNominalDailyRateTons() {
        return nominalDailyRateTons;
    }

    public void setNominalDailyRateTons(double nominalDailyRateTons) {
        if (nominalDailyRateTons <= 0) {
            throw new IllegalArgumentException("A taxa diária nominal deve ser maior que zero.");
        }
        this.nominalDailyRateTons = nominalDailyRateTons;
    }

    public String getUnitDescription() {
        return unitDescription;
    }

    public void setUnitDescription(String unitDescription) {
        this.unitDescription = unitDescription == null ? "Toneladas (t)" : unitDescription.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CargoItem that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " - " + commodityName + " (" + category.getDisplayName() + ")";
    }
}
