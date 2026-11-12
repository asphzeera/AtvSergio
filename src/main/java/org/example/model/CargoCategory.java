package org.example.model;

/**
 * Enumeração dos 11 Perfis de Carga oficiais da Autoridade Portuária de Santos (APS).
 */
public enum CargoCategory {
    GRANEIS_SOLIDOS_VEGETAIS("Granéis Sólidos Vegetais", "SV", "#16a34a"),
    CONTEINERES("Contêineres", "CT", "#2563eb"),
    CELULOSE("Celulose", "CL", "#0d9488"),
    LIQUIDOS_QUIMICOS_COMBUSTIVEIS("Líquidos Químicos/Combustíveis", "LQ", "#dc2626"),
    PRODUTOS_SIDERURGICOS("Produtos Siderúrgicos", "SD", "#475569"),
    GRANEIS_MINERAIS("Granéis Minerais", "MN", "#ca8a04"),
    SUCOS_CITRICOS("Sucos Cítricos", "SC", "#ea580c"),
    MULTIPROPOSITO("Multipropósito", "MP", "#7c3aed"),
    VEICULOS_RORO("Veículos (Ro-Ro)", "RR", "#0284c7"),
    PASSAGEIROS_CRUZEIROS("Passageiros (Cruzeiros)", "PS", "#db2777"),
    CARGA_OFFSHORE("Carga Offshore", "OF", "#4f46e5");

    private final String displayName;
    private final String prefix;
    private final String badgeColor;

    CargoCategory(String displayName, String prefix, String badgeColor) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
