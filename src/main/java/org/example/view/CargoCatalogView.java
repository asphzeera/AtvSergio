package org.example.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.controller.PortController;
import org.example.model.CargoCategory;
import org.example.model.CargoItem;

import java.util.Locale;

/**
 * Tela de Catálogo de Cargas e Perfis Portuários APS (RF007).
 * Exibe e gerencia os 11 perfis APS, commodities, sentido operacional (↓ Descarga, ↑ Carga, ↓↑ Misto)
 * e taxa diária nominal (t/dia) utilizada no cálculo determinístico RN004.
 */
public class CargoCatalogView extends BorderPane {

    private final PortController controller;

    private final TextField codeField = new TextField();
    private final TextField commodityField = new TextField();
    private final ComboBox<CargoCategory> categoryCombo = new ComboBox<>();
    private final ComboBox<String> directionCombo = new ComboBox<>();
    private final TextField dailyRateField = new TextField();
    private final TextField unitField = new TextField();

    private final TextField searchField = new TextField();
    private final Label feedbackBanner = new Label();
    private final TableView<CargoItem> cargoTable = new TableView<>();

    public CargoCatalogView(PortController controller) {
        this.controller = controller;
        getStyleClass().add("view-container");
        setPadding(new Insets(16, 20, 16, 20));

        VBox topSection = new VBox(12);
        topSection.getChildren().addAll(buildHeader(), buildFormCard());
        setTop(topSection);

        VBox centerSection = buildTableCard();
        BorderPane.setMargin(centerSection, new Insets(12, 0, 0, 0));
        setCenter(centerSection);
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label title = new Label("Catálogo de Cargas e Perfis Portuários APS (RF007)");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("11 Perfis Operacionais da Autoridade Portuária de Santos, sentido de movimentação e produtividade nominal (t/dia)");
        subtitle.getStyleClass().add("page-subtitle");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label pill = new Label("11 Perfis Oficiais APS");
        pill.getStyleClass().add("info-pill-badge");

        header.getChildren().addAll(titles, spacer, pill);
        return header;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");

        Label formTitle = new Label("Cadastro / Atualização de Perfil e Commodity Portuária (RF007)");
        formTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        codeField.setPromptText("Ex: APS-SV05");
        commodityField.setPromptText("Ex: Farelo de Soja Peletizado");
        categoryCombo.getItems().setAll(CargoCategory.values());
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.setMaxWidth(Double.MAX_VALUE);

        directionCombo.getItems().setAll("↑ Carga", "↓ Descarga", "↓↑ Misto");
        directionCombo.getSelectionModel().selectFirst();
        directionCombo.setMaxWidth(Double.MAX_VALUE);

        dailyRateField.setPromptText("Ex: 16500");
        unitField.setPromptText("Ex: Toneladas Métricas (t)");
        unitField.setText("Toneladas Métricas (t)");

        grid.add(createFieldBox("Código APS *", codeField), 0, 0);
        grid.add(createFieldBox("Commodity / Especificação da Carga *", commodityField), 1, 0);
        grid.add(createFieldBox("Perfil de Carga APS (11 Categorias) *", categoryCombo), 2, 0);

        grid.add(createFieldBox("Sentido Operacional *", directionCombo), 0, 1);
        grid.add(createFieldBox("Taxa Diária Nominal (t/dia — RN004) *", dailyRateField), 1, 1);
        grid.add(createFieldBox("Unidade Operacional Padrão", unitField), 2, 1);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnSave = new Button("Salvar / Atualizar Carga APS");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> handleSaveCargo());

        Button btnClear = new Button("Limpar Campos");
        btnClear.getStyleClass().add("secondary-button");
        btnClear.setOnAction(e -> clearForm());

        feedbackBanner.getStyleClass().add("feedback-banner-hidden");
        HBox.setHgrow(feedbackBanner, Priority.ALWAYS);

        actions.getChildren().addAll(btnSave, btnClear, feedbackBanner);
        card.getChildren().addAll(formTitle, grid, actions);
        return card;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("table-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Tabela de Commodities e Perfis Portuários APS");
        tableTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField.setPromptText("Buscar por commodity, perfil APS ou sentido...");
        searchField.setPrefWidth(300);
        topRow.getChildren().addAll(tableTitle, spacer, searchField);

        FilteredList<CargoItem> filtered = new FilteredList<>(controller.getCargoCatalog(), c -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.trim().toLowerCase();
            filtered.setPredicate(item -> q.isEmpty()
                    || item.getCode().toLowerCase().contains(q)
                    || item.getCommodityName().toLowerCase().contains(q)
                    || item.getCategory().getDisplayName().toLowerCase().contains(q)
                    || item.getOperationalDirection().toLowerCase().contains(q));
        });

        TableColumn<CargoItem, String> colCode = new TableColumn<>("Código APS");
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        colCode.setPrefWidth(100);

        TableColumn<CargoItem, String> colCommodity = new TableColumn<>("Commodity / Tipo de Carga");
        colCommodity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCommodityName()));
        colCommodity.setPrefWidth(260);

        TableColumn<CargoItem, String> colProfile = new TableColumn<>("Perfil Portuário APS");
        colProfile.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory().getDisplayName()));
        colProfile.setPrefWidth(220);

        TableColumn<CargoItem, String> colDir = new TableColumn<>("Sentido Operacional");
        colDir.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOperationalDirection()));
        colDir.setPrefWidth(135);

        TableColumn<CargoItem, String> colRate = new TableColumn<>("Taxa Diária Nominal (t/dia)");
        colRate.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.of("pt", "BR"), "%,.0f t/dia", d.getValue().getNominalDailyRateTons())));
        colRate.setPrefWidth(165);

        TableColumn<CargoItem, String> colUnit = new TableColumn<>("Unidade");
        colUnit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnitDescription()));
        colUnit.setPrefWidth(165);

        TableColumn<CargoItem, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Editar");
            private final Button btnDelete = new Button("Remover");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("table-btn-edit");
                btnDelete.getStyleClass().add("table-btn-delete");

                btnEdit.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> {
                    CargoItem item = getTableView().getItems().get(getIndex());
                    controller.deleteCargoItem(item);
                    showFeedback("Item " + item.getCode() + " removido do catálogo.", true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        cargoTable.getColumns().setAll(colCode, colCommodity, colProfile, colDir, colRate, colUnit, colActions);
        cargoTable.setItems(filtered);
        cargoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(cargoTable, Priority.ALWAYS);

        cargoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) populateForm(newV);
        });

        card.getChildren().addAll(topRow, cargoTable);
        return card;
    }

    private VBox createFieldBox(String labelText, Control control) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private void populateForm(CargoItem item) {
        codeField.setText(item.getCode());
        commodityField.setText(item.getCommodityName());
        categoryCombo.setValue(item.getCategory());
        directionCombo.setValue(item.getOperationalDirection());
        dailyRateField.setText(String.format(Locale.US, "%.0f", item.getNominalDailyRateTons()));
        unitField.setText(item.getUnitDescription());
    }

    private void handleSaveCargo() {
        try {
            double rate = Double.parseDouble(dailyRateField.getText().trim().replace(",", "."));
            CargoItem item = new CargoItem(
                    codeField.getText(),
                    commodityField.getText(),
                    categoryCombo.getValue(),
                    directionCombo.getValue(),
                    rate,
                    unitField.getText()
            );
            controller.saveOrUpdateCargoItem(item);
            cargoTable.refresh();
            showFeedback("Commodity " + item.getCode() + " salva com sucesso no Catálogo APS!", true);
        } catch (NumberFormatException ex) {
            showFeedback("Informe um valor numérico válido para a Taxa Diária Nominal (t/dia).", false);
        } catch (IllegalArgumentException ex) {
            showFeedback(ex.getMessage(), false);
        }
    }

    private void clearForm() {
        codeField.clear();
        commodityField.clear();
        categoryCombo.getSelectionModel().selectFirst();
        directionCombo.getSelectionModel().selectFirst();
        dailyRateField.clear();
        unitField.setText("Toneladas Métricas (t)");
        cargoTable.getSelectionModel().clearSelection();
        feedbackBanner.setText("");
        feedbackBanner.getStyleClass().setAll("feedback-banner-hidden");
    }

    private void showFeedback(String msg, boolean success) {
        feedbackBanner.setText(msg);
        feedbackBanner.getStyleClass().setAll(success ? "feedback-banner-success" : "feedback-banner-error");
    }
}
