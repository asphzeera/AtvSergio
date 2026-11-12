package org.example.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.example.model.*;
import org.example.service.PortDataService;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Controlador central MVC do Gêmeo Digital do Porto de Santos (APS FIPS).
 * Orquestra a comunicação entre as Views JavaFX e o PortDataService.
 */
public class PortController {

    private final PortDataService dataService;
    private final ObjectProperty<ShipSchedule> selectedScheduleProperty = new SimpleObjectProperty<>();

    public PortController(PortDataService dataService) {
        this.dataService = dataService;
        if (!dataService.getShipSchedules().isEmpty()) {
            selectedScheduleProperty.set(dataService.getShipSchedules().getFirst());
        }
    }

    public PortDataService getDataService() {
        return dataService;
    }

    public ObjectProperty<ShipSchedule> selectedScheduleProperty() {
        return selectedScheduleProperty;
    }

    public ShipSchedule getSelectedSchedule() {
        return selectedScheduleProperty.get();
    }

    public void setSelectedSchedule(ShipSchedule schedule) {
        selectedScheduleProperty.set(schedule);
    }

    public ObservableList<Berth> getBerths() {
        return dataService.getBerths();
    }

    public ObservableList<CargoItem> getCargoCatalog() {
        return dataService.getCargoCatalog();
    }

    public ObservableList<RegisteredVessel> getRegisteredVessels() {
        return dataService.getRegisteredVessels();
    }

    public ObservableList<ShipSchedule> getShipSchedules() {
        return dataService.getShipSchedules();
    }

    public ObservableList<TideMeasurement> getActiveTideMeasurements() {
        return dataService.getActiveTideMeasurements();
    }

    public List<String> getAvailableMonths() {
        return dataService.getAvailableMonths();
    }

    public String getActiveMonth() {
        return dataService.getActiveMonth();
    }

    public void setActiveMonth(String monthYear) {
        dataService.setActiveMonth(monthYear);
    }

    public TideTable getActiveTideTable() {
        return dataService.getActiveTideTable();
    }

    public Set<Integer> getCriticalLowTideDays() {
        return dataService.getCriticalLowTideDays();
    }

    public int parseAndSaveTideCsv(File csvFile) {
        return dataService.parseAndSaveTideCsv(csvFile);
    }

    public void updateDayTideCondition(int dayOfMonth, double targetMinHeightMeters) {
        dataService.updateDayTideCondition(dayOfMonth, targetMinHeightMeters);
    }

    public void saveOrUpdateBerth(Berth berth) {
        dataService.saveOrUpdateBerth(berth);
    }

    public int deleteBerthCascade(Berth berth) {
        int removed = dataService.deleteBerthCascade(berth);
        if (selectedScheduleProperty.get() != null &&
                selectedScheduleProperty.get().getBerth().getCode().equalsIgnoreCase(berth.getCode())) {
            selectedScheduleProperty.set(dataService.getShipSchedules().isEmpty() ? null : dataService.getShipSchedules().getFirst());
        }
        return removed;
    }

    public void saveOrUpdateCargoItem(CargoItem item) {
        dataService.saveOrUpdateCargoItem(item);
    }

    public void deleteCargoItem(CargoItem item) {
        dataService.deleteCargoItem(item);
    }

    public void saveOrUpdateVessel(RegisteredVessel vessel) {
        dataService.saveOrUpdateVessel(vessel);
    }

    public void deleteVessel(RegisteredVessel vessel) {
        dataService.deleteVessel(vessel);
    }

    public ShipSchedule scheduleBerthing(Berth berth, RegisteredVessel vessel,
                                         int startDay, int endDay,
                                         double volumeTons, String status,
                                         String commodity, String agency) {
        ShipSchedule created = dataService.scheduleBerthing(
                berth, vessel, startDay, endDay, volumeTons, status, commodity, agency
        );
        selectedScheduleProperty.set(created);
        return created;
    }

    public void cancelSchedule(ShipSchedule schedule) {
        dataService.cancelSchedule(schedule);
        if (selectedScheduleProperty.get() != null && selectedScheduleProperty.get().equals(schedule)) {
            selectedScheduleProperty.set(dataService.getShipSchedules().isEmpty() ? null : dataService.getShipSchedules().getFirst());
        }
    }

    public int getTotalBerthsCount() {
        return dataService.getTotalBerthsCount();
    }

    public int getScheduledVesselsCount() {
        return dataService.getScheduledVesselsCount();
    }

    public double getTotalScheduledTonnage() {
        return dataService.getTotalScheduledTonnage();
    }

    public double getGlobalOccupancyRatePercent() {
        return dataService.getGlobalOccupancyRatePercent();
    }
}
