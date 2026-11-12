package org.example.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.example.controller.PortController;
import org.example.model.TideMeasurement;
import org.example.model.TideTable;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tela de Monitoramento de Tábuas de Marés Astronômicas com Precisão de 30 Minutos (RF010).
 * Contém:
 * - Seletor de mês ativo (Novembro, Setembro ou Junho de 2026)
 * - Botão de Importação de arquivo .csv oficial da Marinha/DHN (BtnImportCsv — RF010)
 * - Editor de condição de maré por dia (1 a 30)
 * - Representação gráfica da curva de marés com linha de corte de maré baixa (<= 0,4m — RN002)
 * - TableView<TideMeasurement> com as 1.440 medições mensais (48 por dia, a cada 30 min).
 */
public class TideMonitoringView extends BorderPane {

    private final PortController controller;

    private final ComboBox<String> monthCombo = new ComboBox<>();
    private final ComboBox<String> dayFilterCombo = new ComboBox<>();
    private final Spinner<Integer> editDaySpinner = new Spinner<>(1, 30, 8);
    private final TextField editHeightField = new TextField("0.32");
    private final ComboBox<String> editConditionCombo = new ComboBox<>();
    private final CheckBox onlyCriticalCheck = new CheckBox("Apenas Maré Baixa Crítica (≤ 0,40m)");

    private final Label feedbackBanner = new Label();
    private final Label criticalDaysSummaryLabel = new Label();
    private final Label readingsCountBadge = new Label();
    private final Canvas tideCurveCanvas = new Canvas(980, 195);
    private final TableView<TideMeasurement> tideTable = new TableView<>();

    public TideMonitoringView(PortController controller) {
        this.controller = controller;
        getStyleClass().add("view-container");
        setPadding(new Insets(14, 20, 14, 20));

        VBox topSection = new VBox(10);
        topSection.getChildren().addAll(
                buildHeader(),
                buildSelectorAndCsvImportCard()
        );
        setTop(topSection);

        VBox centerSection = buildMeasurementsTableCard();
        BorderPane.setMargin(centerSection, new Insets(10, 0, 0, 0));
        setCenter(centerSection);

        controller.getActiveTideMeasurements().addListener((ListChangeListener<TideMeasurement>) c -> refreshView());
        refreshView();
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label title = new Label("Tábuas de Maré por Mês — Precisão de 30 em 30 Minutos (RF010)");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Consulte ou atualize a condição de Maré Alta (Liberada) e Maré Baixa (Bloqueada ≤ 0,40m — RN002) ou importe CSV oficial da DHN");
        subtitle.getStyleClass().add("page-subtitle");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        readingsCountBadge.getStyleClass().add("info-pill-badge");
        header.getChildren().addAll(titles, spacer, readingsCountBadge);
        return header;
    }

    private VBox buildSelectorAndCsvImportCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");

        // Linha 1: Seletores de Mês, Filtro de Dia e Edição de Dia (conforme Figura 14 do DERS)
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);

        monthCombo.getItems().setAll(controller.getAvailableMonths());
        monthCombo.setValue(controller.getActiveMonth());
        monthCombo.setOnAction(e -> {
            if (monthCombo.getValue() != null) {
                controller.setActiveMonth(monthCombo.getValue());
                refreshView();
            }
        });

        dayFilterCombo.getItems().add("Todos os 30 Dias (1.440 med.)");
        for (int d = 1; d <= 30; d++) {
            dayFilterCombo.getItems().add(String.format("Dia %02d", d));
        }
        dayFilterCombo.getSelectionModel().selectFirst();

        editDaySpinner.setPrefWidth(80);
        editDaySpinner.setEditable(true);

        editHeightField.setPrefWidth(80);

        editConditionCombo.getItems().setAll(
                "BAIXA CRÍTICA — Bloqueado Calado > 13,5m (≤ 0,40m)",
                "ALTA — Liberado p/ Atracação (> 0,40m)"
        );
        editConditionCombo.getSelectionModel().selectFirst();
        editConditionCombo.setOnAction(e -> {
            if (editConditionCombo.getSelectionModel().getSelectedIndex() == 0) {
                editHeightField.setText("0.30");
            } else {
                editHeightField.setText("1.65");
            }
        });

        row1.getChildren().addAll(
                createLabeledControl("Selecionar Mês Programado:", monthCombo),
                createLabeledControl("Filtrar Dia do Mês:", dayFilterCombo),
                createLabeledControl("Editar Dia (1–30):", editDaySpinner),
                createLabeledControl("Altura Mínima (m):", editHeightField),
                createLabeledControl("Condição da Maré no Dia:", editConditionCombo)
        );

        // Linha 2: Botões de Ação (Aplicar Condição + Importar CSV/Excel de Marés — BtnImportCsv RF010)
        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);

        Button btnApplyDayCondition = new Button("💾 Aplicar Condição de Maré no Dia");
        btnApplyDayCondition.getStyleClass().add("primary-button");
        btnApplyDayCondition.setOnAction(e -> handleApplyDayCondition());

        Button btnImportCsv = new Button("📥 Importar CSV/Excel de Marés");
        btnImportCsv.setId("BtnImportCsv");
        btnImportCsv.getStyleClass().add("secondary-button");
        btnImportCsv.setOnAction(e -> handleImportCsvFile());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        criticalDaysSummaryLabel.getStyleClass().add("critical-tide-banner-badge");

        row2.getChildren().addAll(btnApplyDayCondition, btnImportCsv, onlyCriticalCheck, spacer, criticalDaysSummaryLabel);

        feedbackBanner.getStyleClass().add("feedback-banner-hidden");

        StackPane canvasContainer = new StackPane(tideCurveCanvas);
        canvasContainer.getStyleClass().add("tide-canvas-container");
        canvasContainer.setPadding(new Insets(4));

        canvasContainer.widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 300) {
                tideCurveCanvas.setWidth(newW.doubleValue() - 20);
                drawTideCurveChart();
            }
        });

        card.getChildren().addAll(row1, row2, feedbackBanner, canvasContainer);
        return card;
    }

    private VBox createLabeledControl(String labelText, Control control) {
        VBox box = new VBox(3);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private void handleApplyDayCondition() {
        try {
            int day = editDaySpinner.getValue();
            double h = Double.parseDouble(editHeightField.getText().trim().replace(",", "."));
            controller.updateDayTideCondition(day, h);
            refreshView();
            showFeedback(String.format(
                    "Condição do Dia %02d (%s) atualizada para altura mínima %.2fm e sincronizada com o Lineup (RN002/RN008).",
                    day, controller.getActiveMonth(), h), true);
        } catch (NumberFormatException ex) {
            showFeedback("Informe um valor numérico válido para a Altura Mínima (m).", false);
        } catch (IllegalArgumentException ex) {
            showFeedback(ex.getMessage(), false);
        }
    }

    private void handleImportCsvFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar Tábua de Marés Oficial (CSV DHN / Marinha do Brasil)");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos CSV de Marés (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("Todos os Arquivos (*.*)", "*.*")
        );
        File initialDir = new File(System.getProperty("user.dir"));
        if (initialDir.exists()) {
            chooser.setInitialDirectory(initialDir);
        }
        File selectedFile = chooser.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (selectedFile != null) {
            try {
                int importedCount = controller.parseAndSaveTideCsv(selectedFile);
                refreshView();
                showFeedback(String.format(
                        "RF010 — Arquivo CSV '%s' importado com sucesso (%d medições processadas para %s)!",
                        selectedFile.getName(), importedCount, controller.getActiveMonth()), true);
            } catch (Exception ex) {
                showFeedback("Erro ao importar CSV: " + ex.getMessage(), false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private VBox buildMeasurementsTableCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("table-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label tableTitle = new Label("Leituras Astronômicas de Maré de 30 em 30 Minutos (1.440 Medições Mensais)");
        tableTitle.getStyleClass().add("section-title");

        FilteredList<TideMeasurement> filteredList = new FilteredList<>(controller.getActiveTideMeasurements(), m -> true);

        Runnable applyFilter = () -> {
            String daySel = dayFilterCombo.getValue();
            boolean onlyCritical = onlyCriticalCheck.isSelected();
            filteredList.setPredicate(m -> {
                boolean matchesDay = true;
                if (daySel != null && daySel.startsWith("Dia ")) {
                    int selectedDay = Integer.parseInt(daySel.substring(4).trim());
                    matchesDay = (m.getDayOfMonth() == selectedDay);
                }
                boolean matchesCrit = !onlyCritical || m.isCriticalLowTide();
                return matchesDay && matchesCrit;
            });
            readingsCountBadge.setText(String.format("Exibindo %d de 1.440 Leituras (%s)",
                    filteredList.size(), controller.getActiveMonth()));
            drawTideCurveChart();
        };

        dayFilterCombo.valueProperty().addListener((obs, o, n) -> applyFilter.run());
        onlyCriticalCheck.selectedProperty().addListener((obs, o, n) -> applyFilter.run());

        TableColumn<TideMeasurement, String> colMonth = new TableColumn<>("Mês de Referência");
        colMonth.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMonthYear()));
        colMonth.setPrefWidth(155);

        TableColumn<TideMeasurement, String> colDay = new TableColumn<>("Dia do Mês");
        colDay.setCellValueFactory(d -> new SimpleStringProperty(String.format("Dia %02d", d.getValue().getDayOfMonth())));
        colDay.setPrefWidth(100);

        TableColumn<TideMeasurement, String> colTime = new TableColumn<>("Horário (30 em 30 min)");
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimeFormatted()));
        colTime.setPrefWidth(140);

        TableColumn<TideMeasurement, String> colHeight = new TableColumn<>("Altura da Maré (m)");
        colHeight.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.of("pt", "BR"), "%.2f m", d.getValue().getHeightMeters())));
        colHeight.setPrefWidth(140);

        TableColumn<TideMeasurement, String> colState = new TableColumn<>("Estado da Maré & Regra do Lineup (RN002)");
        colState.setCellValueFactory(d -> {
            TideMeasurement m = d.getValue();
            String ruleText = m.isCriticalLowTide()
                    ? "⚠️ MARÉ BAIXA CRÍTICA (≤ 0,40m — Bloqueio Calado > 13,5m)"
                    : "🌊 MARÉ ALTA (Linha Azul — Atracação Permitida)";
            return new SimpleStringProperty(ruleText);
        });
        colState.setPrefWidth(360);

        tideTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TideMeasurement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isCriticalLowTide()) {
                    setStyle("-fx-background-color: #fee2e2; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        tideTable.getColumns().setAll(colMonth, colDay, colTime, colHeight, colState);
        tideTable.setItems(filteredList);
        tideTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tideTable, Priority.ALWAYS);

        card.getChildren().addAll(tableTitle, tideTable);
        return card;
    }

    private void showFeedback(String msg, boolean success) {
        feedbackBanner.setText(msg);
        feedbackBanner.getStyleClass().setAll(success ? "feedback-banner-success" : "feedback-banner-error");
    }

    private void refreshView() {
        Set<Integer> critDays = controller.getCriticalLowTideDays();
        String daysStr = critDays.stream()
                .map(d -> String.format("Dia %02d", d))
                .collect(Collectors.joining(", "));
        criticalDaysSummaryLabel.setText("RN002 — Maré Crítica (≤ 0,40m): " + (daysStr.isEmpty() ? "Nenhum" : daysStr));
        readingsCountBadge.setText(String.format("Total Mensal: %d Leituras (%s)",
                controller.getActiveTideMeasurements().size(), controller.getActiveMonth()));
        tideTable.refresh();
        drawTideCurveChart();
    }

    private void drawTideCurveChart() {
        double w = tideCurveCanvas.getWidth();
        double h = tideCurveCanvas.getHeight();
        GraphicsContext gc = tideCurveCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#ffffff"));
        gc.fillRect(0, 0, w, h);

        double padLeft = 52;
        double padRight = 24;
        double padTop = 24;
        double padBottom = 28;
        double plotW = w - padLeft - padRight;
        double plotH = h - padTop - padBottom;

        double maxMeters = 2.0;
        double minMeters = 0.0;

        double yCutOff = padTop + plotH * (1.0 - (TideMeasurement.CRITICAL_LOW_TIDE_LIMIT - minMeters) / (maxMeters - minMeters));
        double yZero = padTop + plotH;

        gc.setFill(Color.web("#fee2e2", 0.72));
        gc.fillRect(padLeft, yCutOff, plotW, yZero - yCutOff);

        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        double[] gridLevels = {0.0, 0.4, 0.8, 1.2, 1.6, 2.0};
        for (double level : gridLevels) {
            double y = padTop + plotH * (1.0 - (level / maxMeters));
            if (Math.abs(level - 0.4) < 0.01) {
                gc.setStroke(Color.web("#dc2626"));
                gc.setLineWidth(1.8);
                gc.setLineDashes(6, 4);
                gc.strokeLine(padLeft, y, padLeft + plotW, y);
                gc.setLineDashes(0);

                gc.setFill(Color.web("#b91c1c"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 11));
                gc.fillText("0,40m", 8, y + 4);
                gc.fillText("LINHA DE CORTE MARÉ BAIXA CRÍTICA ≤ 0,40m (RN002 — Bloqueio Calado > 13,5m)", padLeft + 10, y - 5);
            } else {
                gc.setStroke(Color.web("#e2e8f0"));
                gc.setLineWidth(1.0);
                gc.strokeLine(padLeft, y, padLeft + plotW, y);

                gc.setFill(Color.web("#64748b"));
                gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
                gc.fillText(String.format(Locale.of("pt", "BR"), "%.1fm", level), 12, y + 4);
            }
        }

        TideTable table = controller.getActiveTideTable();
        String daySel = dayFilterCombo.getValue();

        if (daySel != null && daySel.startsWith("Dia ")) {
            int selectedDay = Integer.parseInt(daySel.substring(4).trim());
            List<TideMeasurement> dayReadings = table.getMeasurements().stream()
                    .filter(m -> m.getDayOfMonth() == selectedDay)
                    .toList();

            gc.setStroke(Color.web("#0284c7"));
            gc.setLineWidth(2.2);
            gc.beginPath();
            for (int i = 0; i < dayReadings.size(); i++) {
                TideMeasurement m = dayReadings.get(i);
                double x = padLeft + (i / 47.0) * plotW;
                double y = padTop + plotH * (1.0 - (m.getHeightMeters() / maxMeters));
                if (i == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.stroke();

            for (int i = 0; i < dayReadings.size(); i++) {
                TideMeasurement m = dayReadings.get(i);
                double x = padLeft + (i / 47.0) * plotW;
                double y = padTop + plotH * (1.0 - (m.getHeightMeters() / maxMeters));
                if (m.isCriticalLowTide()) {
                    gc.setFill(Color.web("#dc2626"));
                    gc.fillOval(x - 3.5, y - 3.5, 7, 7);
                }
                if (i % 4 == 0) {
                    gc.setFill(Color.web("#475569"));
                    gc.setFont(Font.font("System", 9.5));
                    gc.fillText(m.getTimeFormatted(), x - 12, h - 8);
                }
            }
        } else {
            gc.setStroke(Color.web("#93c5fd"));
            gc.setLineWidth(1.5);
            gc.beginPath();
            for (int day = 1; day <= 30; day++) {
                double maxH = table.getMaxTideForDay(day);
                double x = padLeft + ((day - 0.5) / 30.0) * plotW;
                double y = padTop + plotH * (1.0 - (maxH / maxMeters));
                if (day == 1) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.stroke();

            gc.setStroke(Color.web("#0284c7"));
            gc.setLineWidth(2.4);
            gc.beginPath();
            for (int day = 1; day <= 30; day++) {
                double minH = table.getMinTideForDay(day);
                double x = padLeft + ((day - 0.5) / 30.0) * plotW;
                double y = padTop + plotH * (1.0 - (minH / maxMeters));
                if (day == 1) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.stroke();

            for (int day = 1; day <= 30; day++) {
                double minH = table.getMinTideForDay(day);
                double x = padLeft + ((day - 0.5) / 30.0) * plotW;
                double y = padTop + plotH * (1.0 - (minH / maxMeters));
                boolean isCrit = table.isCriticalLowTideDay(day);

                if (isCrit) {
                    gc.setFill(Color.web("#dc2626"));
                    gc.fillOval(x - 5, y - 5, 10, 10);
                    gc.setFont(Font.font("System", FontWeight.BOLD, 10.5));
                    gc.fillText(String.format("D%02d (%.2fm)", day, minH), x - 24, y - 8);
                } else {
                    gc.setFill(Color.web("#0284c7"));
                    gc.fillOval(x - 3, y - 3, 6, 6);
                }

                gc.setFill(isCrit ? Color.web("#dc2626") : Color.web("#475569"));
                gc.setFont(Font.font("System", isCrit ? FontWeight.BOLD : FontWeight.NORMAL, 10));
                gc.fillText(String.format("%02d", day), x - 6, h - 8);
            }
        }

        gc.setFill(Color.web("#0f172a"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText("Curva Astronômica de Baixa-Mar & Preamar — " + controller.getActiveMonth(), padLeft, 15);
    }
}
