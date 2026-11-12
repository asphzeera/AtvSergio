package org.example.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.controller.PortController;
import org.example.model.Berth;
import org.example.model.RegisteredVessel;
import org.example.model.ShipSchedule;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Tela de Programação de Chegadas e Atracações no Lineup (RF011) e
 * Auditoria / Histórico de Atracações no Porto de Santos (RF012).
 * Aplica rigorosamente:
 * - RN001: Anti-Sobreposição Temporal em Berços Acostáveis: (F_A <= I_B) v (F_B <= I_A)
 * - RN002: Bloqueio Hidrodinâmico em Dias de Maré Baixa Crítica (<= 0,4m) para calado > 13,5m
 * - RN003: Compatibilidade Específica de Perfil de Carga
 * - RN004: Cálculo Determinístico da Duração de Atracação
 * - RN005: Cores Normatizadas de Status (#e74c3c, #e67e22, #2ecc71).
 */
public class ShipScheduleView extends BorderPane {

    private final PortController controller;

    private final ComboBox<Berth> berthCombo = new ComboBox<>();
    private final ComboBox<RegisteredVessel> vesselCombo = new ComboBox<>();
    private final Spinner<Integer> startDaySpinner = new Spinner<>(1, 29, 9);
    private final Spinner<Integer> endDaySpinner = new Spinner<>(2, 30, 13);
    private final TextField volumeTonsField = new TextField();
    private final ComboBox<String> statusCombo = new ComboBox<>();
    private final TextField commodityField = new TextField();
    private final TextField agencyField = new TextField();

    private final Label rn004PreviewLabel = new Label();
    private final Label validationAlertBanner = new Label();
    private final TextField searchField = new TextField();
    private final Label totalSchedulesBadge = new Label();
    private final TableView<ShipSchedule> scheduleTable = new TableView<>();

    public ShipScheduleView(PortController controller) {
        this.controller = controller;
        getStyleClass().add("view-container");
        setPadding(new Insets(16, 20, 16, 20));

        VBox topSection = new VBox(12);
        topSection.getChildren().addAll(buildHeader(), buildSchedulingFormCard());
        setTop(topSection);

        VBox centerSection = buildHistoryTableCard();
        BorderPane.setMargin(centerSection, new Insets(12, 0, 0, 0));
        setCenter(centerSection);

        updateSchedulesCount();
        controller.getShipSchedules().addListener((ListChangeListener<ShipSchedule>) c -> updateSchedulesCount());
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label title = new Label("Programação de Chegadas e Auditoria de Atracações (RF011 / RF012)");
        title.getStyleClass().add("page-title");

        String critDays = controller.getCriticalLowTideDays().stream()
                .map(d -> String.format("%02d", d))
                .collect(Collectors.joining(", "));
        Label subtitle = new Label(String.format(
                "Motor de Validação Algorítmica Ativo: RN001 (Anti-Sobreposição) • RN002 (Bloqueio > 13,5m nos dias %s) • RN003 (Perfil Carga) • RN004 (Duração)",
                critDays));
        subtitle.getStyleClass().add("page-subtitle");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalSchedulesBadge.getStyleClass().add("info-pill-badge");
        header.getChildren().addAll(titles, spacer, totalSchedulesBadge);
        return header;
    }

    private VBox buildSchedulingFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label formTitle = new Label("Agendamento de Atracação no Lineup Portuário (RF011)");
        formTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        rn004PreviewLabel.getStyleClass().add("rn004-badge");
        titleRow.getChildren().addAll(formTitle, spacer, rn004PreviewLabel);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        berthCombo.setItems(controller.getBerths());
        berthCombo.getSelectionModel().selectFirst();
        berthCombo.setMaxWidth(Double.MAX_VALUE);

        vesselCombo.setItems(controller.getRegisteredVessels());
        vesselCombo.getSelectionModel().selectFirst();
        vesselCombo.setMaxWidth(Double.MAX_VALUE);

        startDaySpinner.setEditable(true);
        startDaySpinner.setMaxWidth(Double.MAX_VALUE);
        endDaySpinner.setEditable(true);
        endDaySpinner.setMaxWidth(Double.MAX_VALUE);

        volumeTonsField.setPromptText("Ex: 68000");
        volumeTonsField.setText("68000");

        statusCombo.getItems().setAll(
                ShipSchedule.STATUS_PROGRAMADO,
                ShipSchedule.STATUS_CONFIRMADO,
                ShipSchedule.STATUS_OPERANDO
        );
        statusCombo.getSelectionModel().selectFirst();
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        commodityField.setPromptText("Ex: Soja em Grãos (Exportação)");
        commodityField.setText("Soja em Grãos (Exportação)");

        agencyField.setPromptText("Ex: Wilson Sons Agência Marítima");
        agencyField.setText("Wilson Sons Agência");

        grid.add(createFieldBox("Berço de Atracação (APS) *", berthCombo), 0, 0, 2, 1);
        grid.add(createFieldBox("Navio Mercante (Frota IMO) *", vesselCombo), 2, 0, 2, 1);

        grid.add(createFieldBox("Dia Início (1 a 29) *", startDaySpinner), 0, 1);
        grid.add(createFieldBox("Dia Fim (2 a 30) *", endDaySpinner), 1, 1);
        grid.add(createFieldBox("Volume de Carga (Toneladas) *", volumeTonsField), 2, 1);
        grid.add(createFieldBox("Status Operacional (RN005) *", statusCombo), 3, 1);

        grid.add(createFieldBox("Commodity / Lote Embarcado", commodityField), 0, 2, 2, 1);
        grid.add(createFieldBox("Agência de Navegação Responsável", agencyField), 2, 2, 2, 1);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Atualização automática da estimativa RN004
        Runnable updateRn004Preview = () -> {
            Berth b = berthCombo.getValue();
            try {
                double vol = Double.parseDouble(volumeTonsField.getText().trim().replace(",", "."));
                if (b != null && vol > 0) {
                    int days = b.calculateStayDurationDays(vol);
                    rn004PreviewLabel.setText(String.format(
                            "RN004 — Duração Determinística: ⌈%,.0f t / %,.0f t/dia⌉ = %d dias sugeridos",
                            vol, b.getNominalDailyRateTons(), days));
                }
            } catch (NumberFormatException ignored) {
                rn004PreviewLabel.setText("RN004 — Informe a tonelagem para calcular duração");
            }
        };

        berthCombo.valueProperty().addListener((obs, o, n) -> updateRn004Preview.run());
        volumeTonsField.textProperty().addListener((obs, o, n) -> updateRn004Preview.run());
        updateRn004Preview.run();

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnSchedule = new Button("Programar Atracação no Lineup (Validar RN001 / RN002 / RN003)");
        btnSchedule.getStyleClass().add("primary-button");
        btnSchedule.setOnAction(e -> handleCreateSchedule());

        Button btnApplyRn004 = new Button("Aplicar Duração RN004 no Dia Fim");
        btnApplyRn004.getStyleClass().add("secondary-button");
        btnApplyRn004.setOnAction(e -> applyDeterministicDurationRn004());

        validationAlertBanner.setWrapText(true);
        validationAlertBanner.getStyleClass().add("feedback-banner-hidden");
        HBox.setHgrow(validationAlertBanner, Priority.ALWAYS);

        actions.getChildren().addAll(btnSchedule, btnApplyRn004);
        card.getChildren().addAll(titleRow, grid, actions, validationAlertBanner);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox buildHistoryTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("table-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Auditoria e Histórico de Atracações Programadas no Mês (RF012)");
        tableTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField.setPromptText("Filtrar por berço, navio, IMO, commodity ou status...");
        searchField.setPrefWidth(340);
        topBar.getChildren().addAll(tableTitle, spacer, searchField);

        FilteredList<ShipSchedule> filtered = new FilteredList<>(controller.getShipSchedules(), s -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.trim().toLowerCase();
            filtered.setPredicate(s -> q.isEmpty()
                    || s.getId().toLowerCase().contains(q)
                    || s.getBerth().getCode().toLowerCase().contains(q)
                    || s.getBerth().getName().toLowerCase().contains(q)
                    || s.getVessel().getName().toLowerCase().contains(q)
                    || s.getVessel().getImoCode().toLowerCase().contains(q)
                    || s.getOperationalStatus().toLowerCase().contains(q)
                    || s.getCargoCommodity().toLowerCase().contains(q));
        });

        TableColumn<ShipSchedule, String> colId = new TableColumn<>("Protocolo");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colId.setPrefWidth(115);

        TableColumn<ShipSchedule, String> colBerth = new TableColumn<>("Berço APS");
        colBerth.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getBerth().getCode() + " — " + d.getValue().getBerth().getName()));
        colBerth.setPrefWidth(215);

        TableColumn<ShipSchedule, String> colVessel = new TableColumn<>("Navio Mercante (IMO)");
        colVessel.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getVessel().getName() + " (" + d.getValue().getVessel().getImoCode() + ")"));
        colVessel.setPrefWidth(220);

        TableColumn<ShipSchedule, String> colDraft = new TableColumn<>("Calado Navio");
        colDraft.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.of("pt", "BR"), "%.1f m", d.getValue().getVessel().getMaxOperationalDraft())));
        colDraft.setPrefWidth(95);

        TableColumn<ShipSchedule, String> colWindow = new TableColumn<>("Janela Temporal");
        colWindow.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("Dia %02d → Dia %02d (%dd)",
                        d.getValue().getStartDay(), d.getValue().getEndDay(), d.getValue().getDurationDays())));
        colWindow.setPrefWidth(145);

        TableColumn<ShipSchedule, String> colVolume = new TableColumn<>("Tonelagem (t)");
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.of("pt", "BR"));
        colVolume.setCellValueFactory(d -> new SimpleStringProperty(nf.format(d.getValue().getCargoVolumeTons()) + " t"));
        colVolume.setPrefWidth(115);

        TableColumn<ShipSchedule, String> colCommodity = new TableColumn<>("Commodity / Carga");
        colCommodity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCargoCommodity()));
        colCommodity.setPrefWidth(165);

        TableColumn<ShipSchedule, String> colStatus = new TableColumn<>("Status (RN005)");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOperationalStatus()));
        colStatus.setPrefWidth(150);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ShipSchedule sched = getTableRow().getItem();
                    Label pill = new Label(item);
                    pill.setStyle("-fx-background-color: " + sched.getStatusHexColor() + "; "
                            + "-fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(pill);
                    setText(null);
                }
            }
        });

        TableColumn<ShipSchedule, Void> colAction = new TableColumn<>("Cancelar (RF012)");
        colAction.setPrefWidth(125);
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnCancel = new Button("Cancelar Escala");

            {
                btnCancel.getStyleClass().add("table-btn-delete");
                btnCancel.setOnAction(e -> {
                    ShipSchedule s = getTableView().getItems().get(getIndex());
                    controller.cancelSchedule(s);
                    showBanner("Escala " + s.getId() + " (" + s.getVessel().getName() + ") cancelada e removida do Lineup (RN008).", true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnCancel);
            }
        });

        scheduleTable.getColumns().setAll(
                colId, colBerth, colVessel, colDraft, colWindow, colVolume, colCommodity, colStatus, colAction
        );
        scheduleTable.setItems(filtered);
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(scheduleTable, Priority.ALWAYS);

        card.getChildren().addAll(topBar, scheduleTable);
        return card;
    }

    private VBox createFieldBox(String labelText, Control control) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private void applyDeterministicDurationRn004() {
        Berth b = berthCombo.getValue();
        if (b == null) return;
        try {
            double vol = Double.parseDouble(volumeTonsField.getText().trim().replace(",", "."));
            int duration = b.calculateStayDurationDays(vol);
            int start = startDaySpinner.getValue();
            int suggestedEnd = Math.min(30, start + duration);
            endDaySpinner.getValueFactory().setValue(suggestedEnd);
            showBanner(String.format(
                    "RN004 aplicado: Para %,.0f t no berço %s (taxa %,.0f t/dia), duração calculada = %d dias (Dia %02d ao Dia %02d).",
                    vol, b.getCode(), b.getNominalDailyRateTons(), duration, start, suggestedEnd), true);
        } catch (NumberFormatException ex) {
            showBanner("Informe uma tonelagem numérica válida antes de calcular a duração RN004.", false);
        }
    }

    private void handleCreateSchedule() {
        try {
            double volume = Double.parseDouble(volumeTonsField.getText().trim().replace(",", "."));
            int startDay = startDaySpinner.getValue();
            int endDay = endDaySpinner.getValue();

            ShipSchedule created = controller.scheduleBerthing(
                    berthCombo.getValue(),
                    vesselCombo.getValue(),
                    startDay,
                    endDay,
                    volume,
                    statusCombo.getValue(),
                    commodityField.getText(),
                    agencyField.getText()
            );

            scheduleTable.refresh();
            showBanner(String.format(
                    "Escala %s agendada com sucesso! Navio %s alocado no Berço %s [Dia %02d a Dia %02d] e refletido instantaneamente no Lineup (RN008).",
                    created.getId(), created.getVessel().getName(), created.getBerth().getCode(),
                    created.getStartDay(), created.getEndDay()
            ), true);
        } catch (NumberFormatException ex) {
            showBanner("Erro de formato numérico: informe um valor válido para o Volume em Toneladas.", false);
        } catch (IllegalArgumentException ex) {
            showBanner(ex.getMessage(), false);
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Bloqueio Operacional — Regras de Negócio APS");
            alert.setHeaderText("Agendamento Impedido pelo Motor de Validação Marítima");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void showBanner(String message, boolean success) {
        validationAlertBanner.setText(message);
        validationAlertBanner.getStyleClass().setAll(success ? "feedback-banner-success" : "feedback-banner-error");
    }

    private void updateSchedulesCount() {
        totalSchedulesBadge.setText("Escalas Programadas no Mês: " + controller.getShipSchedules().size());
    }
}
