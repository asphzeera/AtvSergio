package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import org.example.controller.PortController;
import org.example.model.Berth;
import org.example.model.ShipSchedule;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Componente visual de um Berço no Lineup Marítimo (RF001 a RF004).
 * Altura expandida (215px) para proporcionar rolagem vertical suave no ScrollPane (RF002),
 * exibindo cabeçalho completo do berço, grade temporal de 30 dias, barras detalhadas de navios
 * com cores normatizadas RN005 (#e74c3c, #e67e22, #2ecc71), onda de maré e régua inferior.
 */
public class BerthChartCard extends HBox {

    public static final double LEFT_INFO_WIDTH = 265.0;
    public static final double DAY_COLUMN_WIDTH = 34.0;
    public static final double CARD_HEIGHT = 215.0;

    private final Berth berth;
    private final List<ShipSchedule> schedules;
    private final Set<Integer> criticalLowTideDays;
    private final PortController controller;

    public BerthChartCard(int indexNumber,
                          Berth berth,
                          List<ShipSchedule> schedules,
                          Set<Integer> criticalLowTideDays,
                          PortController controller) {
        this.berth = berth;
        this.schedules = schedules;
        this.criticalLowTideDays = criticalLowTideDays;
        this.controller = controller;

        getStyleClass().add("berth-chart-card");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(0);
        setMinHeight(CARD_HEIGHT);
        setPrefHeight(CARD_HEIGHT);
        setMaxHeight(CARD_HEIGHT);

        VBox infoBox = buildBerthInfoColumn(indexNumber);
        Pane timelinePane = buildTimelineGrid();

        getChildren().addAll(infoBox, timelinePane);
    }

    private VBox buildBerthInfoColumn(int indexNumber) {
        VBox box = new VBox(6);
        box.getStyleClass().add("berth-info-column");
        box.setMinWidth(LEFT_INFO_WIDTH);
        box.setPrefWidth(LEFT_INFO_WIDTH);
        box.setMaxWidth(LEFT_INFO_WIDTH);
        box.setMinHeight(CARD_HEIGHT - 2);
        box.setPrefHeight(CARD_HEIGHT - 2);
        box.setPadding(new Insets(12, 12, 12, 12));
        box.setAlignment(Pos.TOP_LEFT);

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label indexBadge = new Label(String.format("Berço %02d", indexNumber));
        indexBadge.getStyleClass().add("berth-index-badge");

        Label codeLabel = new Label("— " + berth.getCode());
        codeLabel.getStyleClass().add("berth-code-label");

        topRow.getChildren().addAll(indexBadge, codeLabel);

        Label categoryBadge = new Label(berth.getCargoCategory().getDisplayName());
        categoryBadge.getStyleClass().add("cargo-category-pill");
        categoryBadge.setStyle("-fx-background-color: " + berth.getCargoCategory().getBadgeColor() + "22; "
                + "-fx-text-fill: " + berth.getCargoCategory().getBadgeColor() + "; "
                + "-fx-border-color: " + berth.getCargoCategory().getBadgeColor() + "66;");

        Label nameLabel = new Label(berth.getName());
        nameLabel.getStyleClass().add("berth-name-label");
        nameLabel.setWrapText(true);

        Label sectorLabel = new Label(berth.getSector());
        sectorLabel.getStyleClass().add("berth-meta-label");
        sectorLabel.setWrapText(true);

        Label metaLabel = new Label(String.format("Calado Máx: %.1fm • Taxa: %,.0f t/dia",
                berth.getMaxDraft(), berth.getNominalDailyRateTons()));
        metaLabel.getStyleClass().add("berth-meta-label");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox bottomBadges = new HBox(8);
        bottomBadges.setAlignment(Pos.CENTER_LEFT);

        Label shipsCountBadge = new Label(String.format("%d navio(s) no mês", schedules.size()));
        shipsCountBadge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; "
                + "-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4px;");

        Label lengthBadge = new Label(String.format("Cais: %.0fm", berth.getLengthMeters()));
        lengthBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; "
                + "-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4px;");

        bottomBadges.getChildren().addAll(shipsCountBadge, lengthBadge);

        box.getChildren().addAll(topRow, categoryBadge, nameLabel, sectorLabel, metaLabel, spacer, bottomBadges);
        return box;
    }

    private Pane buildTimelineGrid() {
        Pane container = new Pane();
        double totalWidth = 30 * DAY_COLUMN_WIDTH;
        double gridHeight = CARD_HEIGHT - 2;
        container.setMinWidth(totalWidth);
        container.setPrefWidth(totalWidth);
        container.setMaxWidth(totalWidth);
        container.setMinHeight(gridHeight);
        container.setPrefHeight(gridHeight);

        // 1. Colunas dos 30 dias do mês com destaque visual para dias de maré baixa crítica (RN002)
        for (int day = 1; day <= 30; day++) {
            Region dayCell = new Region();
            double x = (day - 1) * DAY_COLUMN_WIDTH;
            dayCell.setLayoutX(x);
            dayCell.setLayoutY(0);
            dayCell.setPrefSize(DAY_COLUMN_WIDTH, gridHeight);

            boolean isCritical = criticalLowTideDays.contains(day);
            if (isCritical) {
                dayCell.getStyleClass().add("timeline-day-cell-critical");
            } else {
                dayCell.getStyleClass().add(day % 2 == 0 ? "timeline-day-cell-even" : "timeline-day-cell-odd");
            }
            container.getChildren().add(dayCell);

            // Rótulo do dia na base do cartão (01 a 30) como no protótipo do DERS
            Label dayFooterLbl = new Label(String.format("%02d", day));
            dayFooterLbl.setLayoutX(x + 9);
            dayFooterLbl.setLayoutY(gridHeight - 20);
            dayFooterLbl.setStyle(isCritical
                    ? "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #dc2626;"
                    : "-fx-font-size: 9.5px; -fx-text-fill: #64748b;");
            container.getChildren().add(dayFooterLbl);
        }

        // 2. Linha d'água senoidal estilizada na base de cada cartão de berço (como nas Figuras 5 a 8 do DERS)
        Line waterCutLine = new Line(0, gridHeight - 30, totalWidth, gridHeight - 30);
        waterCutLine.setStroke(Color.web("#94a3b8", 0.55));
        waterCutLine.getStrokeDashArray().addAll(4.0, 4.0);
        container.getChildren().add(waterCutLine);

        Polyline tideWave = new Polyline();
        tideWave.setStroke(Color.web("#0284c7", 0.85));
        tideWave.setStrokeWidth(1.8);
        for (int px = 0; px <= (int) totalWidth; px += 4) {
            double dayPos = (px / DAY_COLUMN_WIDTH) + 1.0;
            int dayInt = Math.min(30, Math.max(1, (int) Math.floor(dayPos)));
            boolean crit = criticalLowTideDays.contains(dayInt);
            double amp = crit ? 6.5 : 4.0;
            double base = crit ? (gridHeight - 26) : (gridHeight - 33);
            double y = base + amp * Math.sin(px * 0.18);
            tideWave.getPoints().addAll((double) px, y);
        }
        container.getChildren().add(tideWave);

        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.of("pt", "BR"));

        // 3. Barras horizontais expandidas dos navios programados neste berço
        for (ShipSchedule schedule : schedules) {
            int start = Math.max(1, schedule.getStartDay());
            int end = Math.min(30, schedule.getEndDay());
            int spanDays = Math.max(1, end - start + 1);

            double x = (start - 1) * DAY_COLUMN_WIDTH + 2;
            double width = spanDays * DAY_COLUMN_WIDTH - 4;
            double barHeight = 138.0;

            VBox shipBar = new VBox(4);
            shipBar.setLayoutX(x);
            shipBar.setLayoutY(14);
            shipBar.setPrefSize(width, barHeight);
            shipBar.setMinSize(width, barHeight);
            shipBar.setMaxSize(width, barHeight);
            shipBar.setPadding(new Insets(8, 9, 8, 9));
            shipBar.setAlignment(Pos.TOP_LEFT);
            shipBar.setCursor(Cursor.HAND);
            shipBar.getStyleClass().add("ship-schedule-bar");

            String hexColor = schedule.getStatusHexColor();
            boolean isSelected = controller.getSelectedSchedule() != null
                    && controller.getSelectedSchedule().equals(schedule);

            String borderStyle = isSelected
                    ? "-fx-border-color: #0f172a; -fx-border-width: 3px;"
                    : "-fx-border-color: rgba(255,255,255,0.55); -fx-border-width: 1.2px;";

            shipBar.setStyle("-fx-background-color: " + hexColor + "; "
                    + "-fx-background-radius: 7px; -fx-border-radius: 7px; " + borderStyle);

            Label vesselTitle = new Label(String.format("🚢 %s (Dia %02d–%02d)",
                    schedule.getVessel().getName(), schedule.getStartDay(), schedule.getEndDay()));
            vesselTitle.getStyleClass().add("ship-bar-title");

            Label imoAndClassLbl = new Label(String.format("%s • %s",
                    schedule.getVessel().getImoCode(), schedule.getVessel().getVesselClass()));
            imoAndClassLbl.getStyleClass().add("ship-bar-subtitle");

            Label cargoLbl = new Label(String.format("↓↑ %s", schedule.getCargoCommodity()));
            cargoLbl.getStyleClass().add("ship-bar-subtitle");

            Label volumeAndDraftLbl = new Label(String.format("Vol: %s t • Calado: %.1fm",
                    nf.format(schedule.getCargoVolumeTons()),
                    schedule.getVessel().getMaxOperationalDraft()));
            volumeAndDraftLbl.getStyleClass().add("ship-bar-subtitle");

            Region barSpacer = new Region();
            VBox.setVgrow(barSpacer, Priority.ALWAYS);

            Label statusMini = new Label("● " + schedule.getOperationalStatus().toUpperCase());
            statusMini.getStyleClass().add("ship-bar-status");

            shipBar.getChildren().addAll(vesselTitle, imoAndClassLbl, cargoLbl, volumeAndDraftLbl, barSpacer, statusMini);

            Tooltip tooltip = new Tooltip(String.format(
                    "Escala: %s\nNavio: %s (%s)\nClasse: %s | Calado: %.1fm\nBerço: %s (%s)\nJanela: Dia %02d ao Dia %02d (%d dias)\nCarga: %s (%s t)\nStatus: %s\nAgência: %s",
                    schedule.getId(),
                    schedule.getVessel().getName(),
                    schedule.getVessel().getImoCode(),
                    schedule.getVessel().getVesselClass(),
                    schedule.getVessel().getMaxOperationalDraft(),
                    berth.getCode(),
                    berth.getName(),
                    schedule.getStartDay(),
                    schedule.getEndDay(),
                    schedule.getDurationDays(),
                    schedule.getCargoCommodity(),
                    nf.format(schedule.getCargoVolumeTons()),
                    schedule.getOperationalStatus(),
                    schedule.getAgencyName()
            ));
            Tooltip.install(shipBar, tooltip);

            shipBar.setOnMouseClicked(e -> controller.setSelectedSchedule(schedule));

            container.getChildren().add(shipBar);
        }

        return container;
    }
}
