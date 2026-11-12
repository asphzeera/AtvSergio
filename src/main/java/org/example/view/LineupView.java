package org.example.view;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.example.controller.PortController;
import org.example.model.Berth;
import org.example.model.CargoCategory;
import org.example.model.ShipSchedule;
import org.example.model.TideMeasurement;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tela Principal do Lineup Marítimo e Indicadores Globais (RF001, RF002, RF003 e RF004).
 * Inclui:
 * - 4 Cards de KPIs Globais reativos
 * - Barra com os 11 CheckBoxes de Perfil de Carga APS
 * - Régua temporal de 1 a 30 dias com destaque para maré baixa crítica (dias 08, 14 e 22)
 * - ScrollPane vertical com lotes paginados de 5 em 5 berços com cartões altos (215px) para rolagem vertical fluida (RF002)
 * - Botões de paginação com setas limpas sem texto sobreposto (RF003 e RF004)
 * - Rodapé interativo exibindo o resumo completo do navio selecionado no Lineup.
 */
public class LineupView extends BorderPane {

    private static final int BERTHS_PER_PAGE = 5;

    private final PortController controller;
    private final Map<CargoCategory, CheckBox> categoryCheckBoxes = new EnumMap<>(CargoCategory.class);

    // Labels dos KPIs Globais (RF001)
    private final Label subtitleLabel = new Label();
    private final Label kpiTotalBerthsValue = new Label();
    private final Label kpiScheduledVesselsValue = new Label();
    private final Label kpiTotalTonnageValue = new Label();
    private final Label kpiOccupancyRateValue = new Label();

    // Componentes do Gráfico Lineup e Paginação (RF002, RF003, RF004)
    private final VBox berthCardsContainer = new VBox(14);
    private ScrollPane verticalScroll;
    private final HBox rulerHeaderBox = new HBox(0);
    private final HBox paginationButtonsBox = new HBox(6);
    private final Label pageStatusLabel = new Label();

    // Banner de Resumo do Navio Selecionado no Rodapé (RF002)
    private final Label footerShipTitle = new Label();
    private final Label footerShipDetails = new Label();
    private final Label footerShipStatusBadge = new Label();

    private int currentPageIndex = 0; // 0 = Página 1 (Berços 01 a 05)

    public LineupView(PortController controller) {
        this.controller = controller;
        getStyleClass().add("view-container");
        setPadding(new Insets(14, 20, 14, 20));

        VBox topSection = new VBox(10);
        topSection.getChildren().addAll(
                buildHeaderSection(),
                buildGlobalKpisRow(),
                buildCargoFilterPanel()
        );
        setTop(topSection);

        VBox centerSection = buildLineupCenterSection();
        BorderPane.setMargin(centerSection, new Insets(8, 0, 8, 0));
        setCenter(centerSection);

        VBox bottomSection = new VBox(8);
        bottomSection.getChildren().addAll(
                buildPaginationBar(),
                buildSelectedShipFooterBanner()
        );
        setBottom(bottomSection);

        // Suporte a navegação de páginas via teclas de seta Esquerda/Direita (RF003 Fluxo Alternativo A1)
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.RIGHT) {
                int totalPages = Math.max(1, (int) Math.ceil((double) getFilteredBerths().size() / BERTHS_PER_PAGE));
                if (currentPageIndex < totalPages - 1) {
                    goToPage(currentPageIndex + 1);
                }
            } else if (e.getCode() == KeyCode.LEFT) {
                if (currentPageIndex > 0) {
                    goToPage(currentPageIndex - 1);
                }
            }
        });

        registerReactiveListeners();
        refreshAll();
    }

    private HBox buildHeaderSection() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label title = new Label("Lineup Portuário de Santos (APS) — 60 Berços de Atracação");
        title.getStyleClass().add("page-title");

        subtitleLabel.getStyleClass().add("page-subtitle");
        titles.getChildren().addAll(title, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Legenda RN005 de Cores Normatizadas
        HBox legendBox = new HBox(10);
        legendBox.setAlignment(Pos.CENTER_RIGHT);
        legendBox.getStyleClass().add("status-legend-box");
        legendBox.getChildren().addAll(
                createStatusLegendItem("#e74c3c", "Operando / Prioridade"),
                createStatusLegendItem("#e67e22", "Programado"),
                createStatusLegendItem("#2ecc71", "Confirmado"),
                createStatusLegendItem("#fee2e2", "Maré Baixa ≤ 0,4m")
        );

        Button btnImportCsvHeader = new Button("📥 Importar CSV Maré");
        btnImportCsvHeader.getStyleClass().add("secondary-button-sm");
        btnImportCsvHeader.setOnAction(e -> handleImportTideCsvFromLineup());

        header.getChildren().addAll(titles, spacer, legendBox, btnImportCsvHeader);
        return header;
    }

    private void handleImportTideCsvFromLineup() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar Tábua de Marés Oficial (CSV DHN/Marinha)");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Arquivos CSV de Marés (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("Todos os Arquivos (*.*)", "*.*")
        );
        File initialDir = new File(System.getProperty("user.dir"));
        if (initialDir.exists()) {
            chooser.setInitialDirectory(initialDir);
        }
        File file = chooser.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (file != null) {
            try {
                int count = controller.parseAndSaveTideCsv(file);
                refreshAll();
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Importação de Tábua de Marés CSV (RF010)");
                info.setHeaderText("Tábua de Marés Atualizada com Sucesso!");
                info.setContentText(String.format("Arquivo '%s' processado (%d registros lidos) e sincronizado com o Lineup.",
                        file.getName(), count));
                info.showAndWait();
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erro ao Importar CSV");
                err.setHeaderText("Não foi possível importar o arquivo CSV");
                err.setContentText(ex.getMessage());
                err.showAndWait();
            }
        }
    }

    private HBox createStatusLegendItem(String colorHex, String text) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setPrefSize(12, 12);
        dot.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 3px; -fx-border-color: #cbd5e1; -fx-border-radius: 3px;");
        Label lbl = new Label(text);
        lbl.getStyleClass().add("legend-label");
        item.getChildren().addAll(dot, lbl);
        return item;
    }

    private HBox buildGlobalKpisRow() {
        HBox row = new HBox(14);
        row.getChildren().addAll(
                createKpiCard("TOTAL DE BERÇOS (APS)", kpiTotalBerthsValue, "Berços comerciais ativos no DERS", "#2563eb"),
                createKpiCard("NAVIOS PROGRAMADOS", kpiScheduledVesselsValue, "Escalas alocadas no mês ativo", "#0d9488"),
                createKpiCard("TONELAGEM TOTAL MOVIMENTADA", kpiTotalTonnageValue, "Volume acumulado programado (t)", "#ea580c"),
                createKpiCard("TAXA DE OCUPAÇÃO DO LINEUP", kpiOccupancyRateValue, "Ocupação operacional dos berços", "#7c3aed")
        );
        return row;
    }

    private VBox createKpiCard(String titleText, Label valueLabel, String captionText, String accentColor) {
        VBox card = new VBox(2);
        card.getStyleClass().add("kpi-card");
        card.setStyle("-fx-border-color: transparent transparent transparent " + accentColor + "; -fx-border-width: 0 0 0 4px;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label(titleText);
        title.getStyleClass().add("kpi-card-title");

        valueLabel.getStyleClass().add("kpi-card-value");

        Label caption = new Label(captionText);
        caption.getStyleClass().add("kpi-card-caption");

        card.getChildren().addAll(title, valueLabel, caption);
        return card;
    }

    private VBox buildCargoFilterPanel() {
        VBox panel = new VBox(6);
        panel.getStyleClass().add("filter-card");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label filterTitle = new Label("PORTO DE SANTOS — PERFIL DE CARGA (60 BERÇOS APS / 11 CATEGORIAS):");
        filterTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSelectAll = new Button("✓ Todos os Perfis (60)");
        btnSelectAll.getStyleClass().add("secondary-button-sm");
        btnSelectAll.setOnAction(e -> {
            categoryCheckBoxes.values().forEach(cb -> cb.setSelected(true));
            goToPage(0);
        });

        Button btnClearAll = new Button("Limpar Filtros");
        btnClearAll.getStyleClass().add("secondary-button-sm");
        btnClearAll.setOnAction(e -> {
            categoryCheckBoxes.values().forEach(cb -> cb.setSelected(false));
            goToPage(0);
        });

        topRow.getChildren().addAll(filterTitle, spacer, btnSelectAll, btnClearAll);

        FlowPane checkFlow = new FlowPane(12, 5);
        for (CargoCategory category : CargoCategory.values()) {
            CheckBox cb = new CheckBox(category.getDisplayName());
            cb.setSelected(true);
            cb.getStyleClass().add("cargo-filter-checkbox");
            cb.setOnAction(e -> goToPage(0));
            categoryCheckBoxes.put(category, cb);
            checkFlow.getChildren().add(cb);
        }

        panel.getChildren().addAll(topRow, checkFlow);
        return panel;
    }

    private VBox buildLineupCenterSection() {
        VBox wrapper = new VBox(0);
        wrapper.getStyleClass().add("lineup-board-card");
        VBox.setVgrow(wrapper, Priority.ALWAYS);

        buildRulerHeader();

        verticalScroll = new ScrollPane(berthCardsContainer);
        verticalScroll.setFitToWidth(false);
        verticalScroll.setPannable(true);
        verticalScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        verticalScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        verticalScroll.getStyleClass().add("lineup-scroll-pane");
        VBox.setVgrow(verticalScroll, Priority.ALWAYS);

        berthCardsContainer.setPadding(new Insets(10, 8, 14, 8));

        wrapper.getChildren().addAll(rulerHeaderBox, verticalScroll);
        return wrapper;
    }

    private void buildRulerHeader() {
        rulerHeaderBox.getChildren().clear();
        rulerHeaderBox.getStyleClass().add("timeline-ruler-header");
        rulerHeaderBox.setPadding(new Insets(0, 8, 0, 8));

        VBox leftHeader = new VBox(2);
        leftHeader.setMinWidth(BerthChartCard.LEFT_INFO_WIDTH);
        leftHeader.setPrefWidth(BerthChartCard.LEFT_INFO_WIDTH);
        leftHeader.setMaxWidth(BerthChartCard.LEFT_INFO_WIDTH);
        leftHeader.setPadding(new Insets(6, 12, 6, 12));
        leftHeader.setAlignment(Pos.CENTER_LEFT);
        leftHeader.getStyleClass().add("ruler-left-corner");

        Label lblBerth = new Label("BERÇO / TERMINAL APS");
        lblBerth.getStyleClass().add("ruler-corner-title");
        Label lblSub = new Label("Calado Máx • Taxa Nominal");
        lblSub.getStyleClass().add("ruler-corner-subtitle");
        leftHeader.getChildren().addAll(lblBerth, lblSub);

        HBox daysBar = new HBox(0);
        Set<Integer> criticalDays = controller.getCriticalLowTideDays();

        for (int day = 1; day <= 30; day++) {
            VBox dayCol = new VBox(1);
            dayCol.setMinWidth(BerthChartCard.DAY_COLUMN_WIDTH);
            dayCol.setPrefWidth(BerthChartCard.DAY_COLUMN_WIDTH);
            dayCol.setMaxWidth(BerthChartCard.DAY_COLUMN_WIDTH);
            dayCol.setAlignment(Pos.CENTER);
            dayCol.setPadding(new Insets(4, 0, 4, 0));

            boolean isCritical = criticalDays.contains(day);
            dayCol.getStyleClass().add(isCritical ? "ruler-day-cell-critical" : "ruler-day-cell");

            Label dayNum = new Label(String.format("%02d", day));
            dayNum.getStyleClass().add(isCritical ? "ruler-day-number-critical" : "ruler-day-number");

            Label tideTag = new Label(isCritical ? "≤0,4m" : "Nov");
            tideTag.getStyleClass().add(isCritical ? "ruler-day-tag-critical" : "ruler-day-tag");

            if (isCritical) {
                Tooltip.install(dayCol, new Tooltip(String.format(
                        "Dia %02d/%s: ALERTA DE MARÉ BAIXA CRÍTICA (≤ 0,4m)\nRN002: Bloqueio para navios com calado > 13,5m",
                        day, controller.getActiveMonth())));
            }

            dayCol.getChildren().addAll(dayNum, tideTag);
            daysBar.getChildren().add(dayCol);
        }

        rulerHeaderBox.getChildren().addAll(leftHeader, daysBar);
    }

    private HBox buildPaginationBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("pagination-bar");
        bar.setPadding(new Insets(8, 14, 8, 14));

        pageStatusLabel.getStyleClass().add("pagination-status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lotInfo = new Label("Lote por página: 5 berços");
        lotInfo.getStyleClass().add("pagination-title");

        bar.getChildren().addAll(pageStatusLabel, spacer, paginationButtonsBox, lotInfo);
        return bar;
    }

    private HBox buildSelectedShipFooterBanner() {
        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().add("selected-ship-footer");
        banner.setPadding(new Insets(10, 16, 10, 16));

        Label iconLabel = new Label("⚓");
        iconLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #38bdf8;");

        VBox textColumn = new VBox(3);
        HBox.setHgrow(textColumn, Priority.ALWAYS);
        footerShipTitle.getStyleClass().add("footer-ship-title");
        footerShipDetails.getStyleClass().add("footer-ship-details");
        textColumn.getChildren().addAll(footerShipTitle, footerShipDetails);

        footerShipStatusBadge.getStyleClass().add("footer-status-badge");

        banner.getChildren().addAll(iconLabel, textColumn, footerShipStatusBadge);
        return banner;
    }

    private void registerReactiveListeners() {
        controller.getBerths().addListener((ListChangeListener<Berth>) c -> refreshAll());
        controller.getShipSchedules().addListener((ListChangeListener<ShipSchedule>) c -> refreshAll());
        controller.getActiveTideMeasurements().addListener((ListChangeListener<TideMeasurement>) c -> refreshAll());
        controller.selectedScheduleProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectedShipFooter(newVal);
            refreshLineupCards();
        });
    }

    /**
     * Navega diretamente para o índice da página (0-based) e reposiciona a barra de rolagem no topo (RF003).
     */
    public void goToPage(int pageIndex) {
        this.currentPageIndex = Math.max(0, pageIndex);
        refreshLineupCards();
        if (verticalScroll != null) {
            verticalScroll.setVvalue(0.0);
        }
    }

    public void refreshAll() {
        Set<Integer> lowTideDays = controller.getCriticalLowTideDays();
        String criticalDaysText = lowTideDays.stream()
                .map(d -> String.format("%02d", d))
                .collect(Collectors.joining(", "));
        subtitleLabel.setText(String.format(
                "Tábua de Marés Ativa (%s • 1.440 medições de 30/30 min)  •  Dias de Maré Baixa Crítica (≤ 0,4m — RN002): Dias %s",
                controller.getActiveMonth(), criticalDaysText.isEmpty() ? "Nenhum" : criticalDaysText));

        buildRulerHeader();
        updateGlobalKpis();
        refreshLineupCards();
        updateSelectedShipFooter(controller.getSelectedSchedule());
    }

    private void updateGlobalKpis() {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.of("pt", "BR"));
        kpiTotalBerthsValue.setText(getFilteredBerths().size() + " / " + controller.getTotalBerthsCount() + " berços");
        kpiScheduledVesselsValue.setText(controller.getScheduledVesselsCount() + " Embarcações");
        kpiTotalTonnageValue.setText(nf.format(controller.getTotalScheduledTonnage()) + " t");
        kpiOccupancyRateValue.setText(String.format(Locale.of("pt", "BR"), "%.1f%%", controller.getGlobalOccupancyRatePercent()));
    }

    private List<Berth> getFilteredBerths() {
        Set<CargoCategory> activeCategories = categoryCheckBoxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return controller.getBerths().stream()
                .filter(b -> activeCategories.contains(b.getCargoCategory()))
                .toList();
    }

    private void refreshLineupCards() {
        List<Berth> filteredBerths = getFilteredBerths();
        int totalBerths = filteredBerths.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalBerths / BERTHS_PER_PAGE));

        if (currentPageIndex >= totalPages) {
            currentPageIndex = totalPages - 1;
        }
        if (currentPageIndex < 0) {
            currentPageIndex = 0;
        }

        int fromIndex = currentPageIndex * BERTHS_PER_PAGE;
        int toIndex = Math.min(totalBerths, fromIndex + BERTHS_PER_PAGE);

        berthCardsContainer.getChildren().clear();
        Set<Integer> criticalDays = controller.getCriticalLowTideDays();

        if (filteredBerths.isEmpty()) {
            Label empty = new Label("Nenhum berço encontrado para os Perfis de Carga selecionados.");
            empty.getStyleClass().add("empty-state-label");
            berthCardsContainer.getChildren().add(empty);
        } else {
            for (int i = fromIndex; i < toIndex; i++) {
                Berth berth = filteredBerths.get(i);
                List<ShipSchedule> berthSchedules = controller.getShipSchedules().stream()
                        .filter(s -> s.getBerth().getCode().equalsIgnoreCase(berth.getCode()))
                        .sorted(Comparator.comparingInt(ShipSchedule::getStartDay))
                        .toList();

                BerthChartCard card = new BerthChartCard(i + 1, berth, berthSchedules, criticalDays, controller);
                berthCardsContainer.getChildren().add(card);
            }
        }

        updateGlobalKpis();
        rebuildPaginationControls(totalBerths, totalPages, fromIndex, toIndex);
    }

    /**
     * RF003 e RF004 — Barra de paginação utilizando exclusivamente setas nos botões de avanço/recuo
     * ("⏮", "◀", números das páginas "1".."12", "▶", "⏭") sem texto sobreposto.
     */
    private void rebuildPaginationControls(int totalBerths, int totalPages, int fromIndex, int toIndex) {
        paginationButtonsBox.getChildren().clear();
        paginationButtonsBox.setAlignment(Pos.CENTER);

        Button btnFirst = new Button("⏮");
        btnFirst.getStyleClass().add("pagination-arrow-button");
        btnFirst.setTooltip(new Tooltip("Primeira Página (Berços 01 a 05)"));
        btnFirst.setDisable(currentPageIndex == 0);
        btnFirst.setOnAction(e -> goToPage(0));

        Button btnPrev = new Button("◀");
        btnPrev.getStyleClass().add("pagination-arrow-button");
        btnPrev.setTooltip(new Tooltip("Página Anterior"));
        btnPrev.setDisable(currentPageIndex == 0);
        btnPrev.setOnAction(e -> {
            if (currentPageIndex > 0) {
                goToPage(currentPageIndex - 1);
            }
        });

        paginationButtonsBox.getChildren().addAll(btnFirst, btnPrev);

        for (int p = 0; p < totalPages; p++) {
            final int pageIdx = p;
            int startB = p * BERTHS_PER_PAGE + 1;
            int endB = Math.min(totalBerths, (p + 1) * BERTHS_PER_PAGE);

            Button pageBtn = new Button(String.valueOf(p + 1));
            pageBtn.getStyleClass().add(pageIdx == currentPageIndex ? "pagination-button-active" : "pagination-button");
            pageBtn.setTooltip(new Tooltip(String.format("Página %d (Berços %02d a %02d)", p + 1, startB, endB)));
            pageBtn.setOnAction(e -> goToPage(pageIdx));
            paginationButtonsBox.getChildren().add(pageBtn);
        }

        Button btnNext = new Button("▶");
        btnNext.getStyleClass().add("pagination-arrow-button");
        btnNext.setTooltip(new Tooltip("Próxima Página"));
        btnNext.setDisable(currentPageIndex >= totalPages - 1);
        btnNext.setOnAction(e -> {
            if (currentPageIndex < totalPages - 1) {
                goToPage(currentPageIndex + 1);
            }
        });

        Button btnLast = new Button("⏭");
        btnLast.getStyleClass().add("pagination-arrow-button");
        btnLast.setTooltip(new Tooltip(String.format("Última Página (Página %d)", totalPages)));
        btnLast.setDisable(currentPageIndex >= totalPages - 1);
        btnLast.setOnAction(e -> goToPage(totalPages - 1));

        paginationButtonsBox.getChildren().addAll(btnNext, btnLast);

        if (totalBerths == 0) {
            pageStatusLabel.setText("Exibindo 0 berços filtrados");
        } else {
            pageStatusLabel.setText(String.format("Exibindo berços %d a %d de %d filtrados (Página %d de %d)",
                    fromIndex + 1, toIndex, totalBerths, currentPageIndex + 1, totalPages));
        }
    }

    private void updateSelectedShipFooter(ShipSchedule schedule) {
        if (schedule == null) {
            footerShipTitle.setText("Nenhuma escala selecionada no Lineup");
            footerShipDetails.setText("Clique sobre qualquer barra de navio no gráfico acima para inspecionar os dados operacionais da escala.");
            footerShipStatusBadge.setText("AGUARDANDO SELEÇÃO");
            footerShipStatusBadge.setStyle("-fx-background-color: #475569; -fx-text-fill: white;");
            return;
        }

        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.of("pt", "BR"));
        footerShipTitle.setText(String.format(
                "Selecionado: %s (%s)  |  Berço %s — %s",
                schedule.getVessel().getName(),
                schedule.getVessel().getImoCode(),
                schedule.getBerth().getCode(),
                schedule.getBerth().getName()
        ));

        footerShipDetails.setText(String.format(
                "Período: Dia %02d ao Dia %02d (%d dias)  |  ↓↑ Movimentando: %s  |  Volume: %s t  |  Calado Navio: %.1fm (Máx Berço: %.1fm)  |  Status: %s",
                schedule.getStartDay(),
                schedule.getEndDay(),
                schedule.getDurationDays(),
                schedule.getCargoCommodity(),
                nf.format(schedule.getCargoVolumeTons()),
                schedule.getVessel().getMaxOperationalDraft(),
                schedule.getBerth().getMaxDraft(),
                schedule.getOperationalStatus()
        ));

        footerShipStatusBadge.setText(schedule.getOperationalStatus().toUpperCase());
        footerShipStatusBadge.setStyle("-fx-background-color: " + schedule.getStatusHexColor() + "; -fx-text-fill: white;");
    }
}
