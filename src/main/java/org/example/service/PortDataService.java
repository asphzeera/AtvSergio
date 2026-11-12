package org.example.service;

import javafx.collections.ObservableList;
import org.example.model.*;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Interface de serviço de dados do Gêmeo Digital do Porto de Santos.
 * Arquitetura desacoplada em memória (sem dependência de banco de dados no Checkpoint 1).
 */
public interface PortDataService {
    ObservableList<Berth> getBerths();
    ObservableList<CargoItem> getCargoCatalog();
    ObservableList<RegisteredVessel> getRegisteredVessels();
    ObservableList<ShipSchedule> getShipSchedules();
    ObservableList<TideMeasurement> getActiveTideMeasurements();

    List<String> getAvailableMonths();
    String getActiveMonth();
    void setActiveMonth(String monthYear);
    TideTable getActiveTideTable();
    Set<Integer> getCriticalLowTideDays();

    // Operações de Tábua de Marés e Importação CSV (RF010, RNF015)
    int parseAndSaveTideCsv(File csvFile);
    void updateDayTideCondition(int dayOfMonth, double targetMinHeightMeters);

    // Operações de Berço (RF005, RF006, RN008, RN024)
    void saveOrUpdateBerth(Berth berth);
    int deleteBerthCascade(Berth berth);

    // Operações de Catálogo de Cargas (RF007)
    void saveOrUpdateCargoItem(CargoItem item);
    void deleteCargoItem(CargoItem item);

    // Operações de Catálogo de Navios (RF008, RF009, RN007)
    void saveOrUpdateVessel(RegisteredVessel vessel);
    void deleteVessel(RegisteredVessel vessel);

    // Operações de Programação de Atracações (RF011, RF012, RN001, RN002, RN003, RN004)
    ShipSchedule scheduleBerthing(Berth berth, RegisteredVessel vessel, int startDay, int endDay,
                                  double volumeTons, String status, String commodity, String agency);
    void cancelSchedule(ShipSchedule schedule);

    // KPIs Globais (RF001)
    int getTotalBerthsCount();
    int getScheduledVesselsCount();
    double getTotalScheduledTonnage();
    double getGlobalOccupancyRatePercent();
}
