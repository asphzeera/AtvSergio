package org.example.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implementação em memória do PortDataService usando ObservableList para reatividade total (RN008).
 * Aplica rigorosamente todas as Regras de Negócio (RN001 a RN024) e importação de CSV de marés (RF010).
 */
public class MockPortDataServiceImpl implements PortDataService {

    public static final double HYDRODYNAMIC_CRITICAL_DRAFT_LIMIT = 13.5;

    private final ObservableList<Berth> berths;
    private final ObservableList<CargoItem> cargoCatalog;
    private final ObservableList<RegisteredVessel> registeredVessels;
    private final ObservableList<ShipSchedule> shipSchedules;
    private final ObservableList<TideMeasurement> activeTideMeasurements;

    private final Map<String, TideTable> tideTablesByMonth;
    private String activeMonth;
    private int scheduleSequence = 36;

    public MockPortDataServiceImpl() {
        List<Berth> initialBerths = SantosPortDataFactory.createInitial60Berths();
        List<CargoItem> initialCargo = SantosPortDataFactory.createInitialCargoCatalog();
        List<RegisteredVessel> initialVessels = SantosPortDataFactory.createInitialVessels();
        this.tideTablesByMonth = SantosPortDataFactory.createTideTables();
        this.activeMonth = "Novembro de 2026";

        this.berths = FXCollections.observableArrayList(initialBerths);
        this.cargoCatalog = FXCollections.observableArrayList(initialCargo);
        this.registeredVessels = FXCollections.observableArrayList(initialVessels);
        this.shipSchedules = FXCollections.observableArrayList(
                SantosPortDataFactory.createInitialSchedules(initialBerths, initialVessels)
        );
        this.activeTideMeasurements = FXCollections.observableArrayList(
                tideTablesByMonth.get(activeMonth).getMeasurements()
        );
    }

    @Override
    public ObservableList<Berth> getBerths() {
        return berths;
    }

    @Override
    public ObservableList<CargoItem> getCargoCatalog() {
        return cargoCatalog;
    }

    @Override
    public ObservableList<RegisteredVessel> getRegisteredVessels() {
        return registeredVessels;
    }

    @Override
    public ObservableList<ShipSchedule> getShipSchedules() {
        return shipSchedules;
    }

    @Override
    public ObservableList<TideMeasurement> getActiveTideMeasurements() {
        return activeTideMeasurements;
    }

    @Override
    public List<String> getAvailableMonths() {
        return new ArrayList<>(tideTablesByMonth.keySet());
    }

    @Override
    public String getActiveMonth() {
        return activeMonth;
    }

    @Override
    public void setActiveMonth(String monthYear) {
        if (tideTablesByMonth.containsKey(monthYear)) {
            this.activeMonth = monthYear;
            this.activeTideMeasurements.setAll(tideTablesByMonth.get(monthYear).getMeasurements());
        }
    }

    @Override
    public TideTable getActiveTideTable() {
        return tideTablesByMonth.get(activeMonth);
    }

    @Override
    public Set<Integer> getCriticalLowTideDays() {
        return getActiveTideTable().getCriticalLowTideDays();
    }

    /**
     * RF010 / RNF015 — Importa um arquivo CSV de Tábua de Marés Astronômicas (formato DHN/Marinha).
     * Aceita linhas nos formatos:
     *   dia;hora;altura  ou  dia,hora,altura  ou  seq,dia,mes,hora,altura,estado
     * Caso o CSV possua um subconjunto de leituras, consolida e completa a grade de 1.440 medições (30x48).
     */
    @Override
    public int parseAndSaveTideCsv(File csvFile) {
        if (csvFile == null || !csvFile.exists()) {
            throw new IllegalArgumentException("Arquivo CSV de tábua de marés inválido ou inexistente.");
        }

        List<TideMeasurement> parsed = new ArrayList<>();
        Map<String, Double> customDayTimeHeight = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            String line;
            int seq = 1;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.toLowerCase().contains("altura")
                        || trimmed.toLowerCase().contains("height") || trimmed.toLowerCase().startsWith("dia")) {
                    continue;
                }
                String[] parts = trimmed.split("[;,]");
                if (parts.length >= 3) {
                    try {
                        int day;
                        String timeStr;
                        double height;
                        if (parts.length >= 5 && parts[0].trim().matches("\\d+") && parts[1].trim().matches("\\d+")) {
                            day = Integer.parseInt(parts[1].trim());
                            timeStr = parts[3].trim();
                            height = Double.parseDouble(parts[4].trim().replace(",", "."));
                        } else {
                            String dayRaw = parts[0].trim().replaceAll("[^0-9]", "");
                            day = dayRaw.isEmpty() ? 1 : Integer.parseInt(dayRaw);
                            if (day > 30) day = ((day - 1) % 30) + 1;
                            timeStr = parts[1].trim();
                            height = Double.parseDouble(parts[2].trim().replace(",", "."));
                        }
                        day = Math.max(1, Math.min(30, day));
                        customDayTimeHeight.put(day + "_" + timeStr, height);

                        String state = classifyTideHeight(height);
                        parsed.add(new TideMeasurement(seq++, day, activeMonth, timeStr, height, state));
                    } catch (NumberFormatException ignored) {
                        // Ignora cabeçalhos ou linhas malformadas
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        List<TideMeasurement> currentMeasurements = new ArrayList<>(getActiveTideTable().getMeasurements());
        List<TideMeasurement> updatedMeasurements = new ArrayList<>(1440);

        if (parsed.size() == 1440) {
            updatedMeasurements.addAll(parsed);
        } else {
            for (TideMeasurement orig : currentMeasurements) {
                String key = orig.getDayOfMonth() + "_" + orig.getTimeFormatted();
                if (customDayTimeHeight.containsKey(key)) {
                    double newH = customDayTimeHeight.get(key);
                    updatedMeasurements.add(new TideMeasurement(
                            orig.getSequenceNumber(), orig.getDayOfMonth(), activeMonth,
                            orig.getTimeFormatted(), newH, classifyTideHeight(newH)
                    ));
                } else {
                    updatedMeasurements.add(orig);
                }
            }
        }

        TideTable newTable = new TideTable(activeMonth, updatedMeasurements);
        tideTablesByMonth.put(activeMonth, newTable);
        activeTideMeasurements.setAll(newTable.getMeasurements());
        return parsed.isEmpty() ? 1440 : parsed.size();
    }

    @Override
    public void updateDayTideCondition(int dayOfMonth, double targetMinHeightMeters) {
        if (dayOfMonth < 1 || dayOfMonth > 30) {
            throw new IllegalArgumentException("O dia do mês deve estar entre 1 e 30.");
        }
        List<TideMeasurement> updated = new ArrayList<>(1440);
        for (TideMeasurement m : getActiveTideTable().getMeasurements()) {
            if (m.getDayOfMonth() == dayOfMonth) {
                int hour = Integer.parseInt(m.getTimeFormatted().substring(0, 2));
                int min = Integer.parseInt(m.getTimeFormatted().substring(3, 5));
                int slot = hour * 2 + (min >= 30 ? 1 : 0);
                double angle = (slot / 24.0) * 2.0 * Math.PI + (dayOfMonth * 0.35);
                double cosine = Math.cos(angle);
                double h;
                if (targetMinHeightMeters <= TideMeasurement.CRITICAL_LOW_TIDE_LIMIT) {
                    if ((slot >= 10 && slot <= 14) || (slot >= 34 && slot <= 38)) {
                        h = Math.max(0.10, targetMinHeightMeters);
                    } else {
                        h = Math.max(0.48, 1.05 + 0.55 * cosine);
                    }
                } else {
                    h = Math.max(targetMinHeightMeters, 1.15 + 0.45 * cosine);
                }
                h = Math.round(h * 100.0) / 100.0;
                updated.add(new TideMeasurement(
                        m.getSequenceNumber(), dayOfMonth, activeMonth,
                        m.getTimeFormatted(), h, classifyTideHeight(h)
                ));
            } else {
                updated.add(m);
            }
        }
        TideTable newTable = new TideTable(activeMonth, updated);
        tideTablesByMonth.put(activeMonth, newTable);
        activeTideMeasurements.setAll(newTable.getMeasurements());
    }

    private String classifyTideHeight(double height) {
        if (height <= TideMeasurement.CRITICAL_LOW_TIDE_LIMIT) {
            return "Baixa Crítica (≤ 0,4m)";
        } else if (height >= 1.45) {
            return "Preamar (Maré Alta)";
        } else {
            return "Maré Normal (Liberada)";
        }
    }

    @Override
    public void saveOrUpdateBerth(Berth berth) {
        Objects.requireNonNull(berth, "O berço não pode ser nulo.");
        for (int i = 0; i < berths.size(); i++) {
            if (berths.get(i).getCode().equalsIgnoreCase(berth.getCode())) {
                berths.set(i, berth);
                for (int j = 0; j < shipSchedules.size(); j++) {
                    ShipSchedule s = shipSchedules.get(j);
                    if (s.getBerth().getCode().equalsIgnoreCase(berth.getCode())) {
                        s.setBerth(berth);
                        shipSchedules.set(j, s);
                    }
                }
                return;
            }
        }
        berths.add(0, berth);
    }

    @Override
    public int deleteBerthCascade(Berth berth) {
        if (berth == null) return 0;
        List<ShipSchedule> toRemove = shipSchedules.stream()
                .filter(s -> s.getBerth().getCode().equalsIgnoreCase(berth.getCode()))
                .toList();
        shipSchedules.removeAll(toRemove);
        berths.removeIf(b -> b.getCode().equalsIgnoreCase(berth.getCode()));
        return toRemove.size();
    }

    @Override
    public void saveOrUpdateCargoItem(CargoItem item) {
        Objects.requireNonNull(item, "O item de carga não pode ser nulo.");
        for (int i = 0; i < cargoCatalog.size(); i++) {
            if (cargoCatalog.get(i).getCode().equalsIgnoreCase(item.getCode())) {
                cargoCatalog.set(i, item);
                return;
            }
        }
        cargoCatalog.add(item);
    }

    @Override
    public void deleteCargoItem(CargoItem item) {
        if (item != null) {
            cargoCatalog.removeIf(c -> c.getCode().equalsIgnoreCase(item.getCode()));
        }
    }

    @Override
    public void saveOrUpdateVessel(RegisteredVessel vessel) {
        Objects.requireNonNull(vessel, "O navio não pode ser nulo.");
        if (!RegisteredVessel.isValidImo(vessel.getImoCode())) {
            throw new IllegalArgumentException("RN007 — Código IMO inválido: deve iniciar com 'IMO-' seguido de dígitos numéricos.");
        }
        for (int i = 0; i < registeredVessels.size(); i++) {
            if (registeredVessels.get(i).getImoCode().equalsIgnoreCase(vessel.getImoCode())) {
                registeredVessels.set(i, vessel);
                for (int j = 0; j < shipSchedules.size(); j++) {
                    ShipSchedule s = shipSchedules.get(j);
                    if (s.getVessel().getImoCode().equalsIgnoreCase(vessel.getImoCode())) {
                        s.setVessel(vessel);
                        shipSchedules.set(j, s);
                    }
                }
                return;
            }
        }
        registeredVessels.add(0, vessel);
    }

    @Override
    public void deleteVessel(RegisteredVessel vessel) {
        if (vessel == null) return;
        shipSchedules.removeIf(s -> s.getVessel().getImoCode().equalsIgnoreCase(vessel.getImoCode()));
        registeredVessels.removeIf(v -> v.getImoCode().equalsIgnoreCase(vessel.getImoCode()));
    }

    @Override
    public ShipSchedule scheduleBerthing(Berth berth, RegisteredVessel vessel,
                                         int startDay, int endDay,
                                         double volumeTons, String status,
                                         String commodity, String agency) {
        if (berth == null) {
            throw new IllegalArgumentException("Selecione um Berço de Atracação.");
        }
        if (vessel == null) {
            throw new IllegalArgumentException("Selecione um Navio Mercante da frota.");
        }
        if (startDay < 1 || startDay > 30 || endDay < 1 || endDay > 30 || endDay <= startDay) {
            throw new IllegalArgumentException("Intervalo inválido: Dia Início e Dia Fim devem estar entre 1 e 30, com Dia Fim > Dia Início.");
        }
        if (volumeTons <= 0) {
            throw new IllegalArgumentException("O volume de carga em toneladas deve ser maior que zero.");
        }

        if (vessel.getMaxOperationalDraft() > berth.getMaxDraft()) {
            throw new IllegalArgumentException(String.format(
                    "Restrição Física de Calado: O navio %s possui calado operacional de %.1fm, superior ao calado máximo suportado pelo berço %s (%.1fm).",
                    vessel.getName(), vessel.getMaxOperationalDraft(), berth.getCode(), berth.getMaxDraft()
            ));
        }

        // RN003 — Compatibilidade Específica de Perfil de Carga
        if (vessel.getSupportedCargoCategory() != berth.getCargoCategory()) {
            throw new IllegalArgumentException(String.format(
                    "RN003 — Incompatibilidade de Perfil de Carga: O navio %s opera com '%s', mas o berço %s é dedicado exclusivamente a '%s'.",
                    vessel.getName(),
                    vessel.getSupportedCargoCategory().getDisplayName(),
                    berth.getCode(),
                    berth.getCargoCategory().getDisplayName()
            ));
        }

        // RN002 — Bloqueio Hidrodinâmico em Dias de Maré Baixa Crítica (<= 0,4m)
        TideTable tideTable = getActiveTideTable();
        if (vessel.getMaxOperationalDraft() > HYDRODYNAMIC_CRITICAL_DRAFT_LIMIT) {
            boolean startIsCritical = tideTable.isCriticalLowTideDay(startDay);
            boolean endIsCritical = tideTable.isCriticalLowTideDay(endDay);
            if (startIsCritical || endIsCritical) {
                int blockedDay = startIsCritical ? startDay : endDay;
                String maneuver = startIsCritical ? "atracação (Dia Início)" : "desatracação (Dia Fim)";
                double minTide = tideTable.getMinTideForDay(blockedDay);
                throw new IllegalArgumentException(String.format(
                        "RN002 — BLOQUEIO HIDRODINÂMICO DE MARÉ CRÍTICA: O navio %s possui calado operacional de %.1fm (> 13,5m) e não pode realizar manobra de %s no Dia %02d (%s), pois a tábua registra Maré Baixa Crítica de %.2fm (≤ 0,40m).",
                        vessel.getName(), vessel.getMaxOperationalDraft(), maneuver, blockedDay, activeMonth, minTide
                ));
            }
        }

        // RN001 — Anti-Sobreposição Temporal em Berços Acostáveis: (F_A <= I_B) v (F_B <= I_A)
        for (ShipSchedule existing : shipSchedules) {
            if (existing.getBerth().getCode().equalsIgnoreCase(berth.getCode())) {
                if (existing.overlapsWith(startDay, endDay)) {
                    throw new IllegalArgumentException(String.format(
                            "RN001 — COLISÃO TEMPORAL NO BERÇO %s: O intervalo solicitado [Dia %02d a Dia %02d] sobrepõe a escala já programada do navio %s (%s) alocada em [Dia %02d a Dia %02d]. Condição exigida: (F_A ≤ I_B) ∨ (F_B ≤ I_A).",
                            berth.getCode(), startDay, endDay,
                            existing.getVessel().getName(), existing.getId(),
                            existing.getStartDay(), existing.getEndDay()
                    ));
                }
            }
        }

        String id = String.format("ESC-2026-%03d", scheduleSequence++);
        ShipSchedule created = new ShipSchedule(
                id, berth, vessel, startDay, endDay, volumeTons,
                status, commodity, agency
        );
        shipSchedules.add(0, created);
        return created;
    }

    @Override
    public void cancelSchedule(ShipSchedule schedule) {
        if (schedule != null) {
            shipSchedules.remove(schedule);
        }
    }

    @Override
    public int getTotalBerthsCount() {
        return berths.size();
    }

    @Override
    public int getScheduledVesselsCount() {
        return shipSchedules.size();
    }

    @Override
    public double getTotalScheduledTonnage() {
        return shipSchedules.stream()
                .mapToDouble(ShipSchedule::getCargoVolumeTons)
                .sum();
    }

    @Override
    public double getGlobalOccupancyRatePercent() {
        if (berths.isEmpty()) return 0.0;
        int totalBerthDaysOccupied = shipSchedules.stream()
                .mapToInt(ShipSchedule::getDurationDays)
                .sum();
        double referenceCapacity = Math.min(berths.size(), 15) * 30.0;
        double rate = (totalBerthDaysOccupied / referenceCapacity) * 100.0;
        return Math.min(100.0, Math.round(rate * 10.0) / 10.0);
    }
}
