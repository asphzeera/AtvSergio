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
import org.example.model.CargoCategory;

import java.util.Locale;

/**
 * Tela de Cadastro, Edição, Auditoria e Exclusão em Cascata de Berços de Atracação (RF005, RF006 e RN024).
 */
public class BerthManagementView extends BorderPane {

    private final PortController controller;

    private final TextField codeField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField sectorField = new TextField();
    private final TextField maxDraftField = new TextField();
    private final ComboBox<CargoCategory> categoryCombo = new ComboBox<>();
    private final TextField dailyRateField = new TextField();
    private final TextField lengthField = new TextField();

    private final TextField searchField = new TextField();
    private final Label feedbackBanner = new Label();
    private final Label totalCountBadge = new Label();
    private final TableView<Berth> berthTable = new TableView<>();

    public BerthManagementView(PortController controller) {
        this.controller = controller;
        getStyleClass().add("view-container");
        setPadding(new Insets(16, 20, 16, 20));

        VBox topBox = new VBox(12);
        topBox.getChildren().addAll(buildHeader(), buildFormCard());
        setTop(topBox);

        VBox tableBox = buildTableCard();
        BorderPane.setMargin(tableBox, new Insets(12, 0, 0, 0));
        setCenter(tableBox);

        updateTotalBadge();
        controller.getBerths().addListener((ListChangeListener<Berth>) c -> updateTotalBadge());
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label title = new Label("Gestão e Auditoria de Berços de Atracação (RF005 / RF006)");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Cadastro técnico dos 60 berços comerciais da APS com sincronização reativa (RN008) e exclusão em cascata (RN024)");
        subtitle.getStyleClass().add("page-subtitle");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalCountBadge.getStyleClass().add("info-pill-badge");
        header.getChildren().addAll(titles, spacer, totalCountBadge);
        return header;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");

        Label formTitle = new Label("Formulário de Cadastro / Edição de Berço Acostável (RF005)");
        formTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        codeField.setPromptText("Ex: SV-15");
        nameField.setPromptText("Ex: Terminal Graneleiro Ponta da Praia");
        sectorField.setPromptText("Ex: Margem Direita - Outeirinhos");
        maxDraftField.setPromptText("Ex: 14.5");
        categoryCombo.getItems().setAll(CargoCategory.values());
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        dailyRateField.setPromptText("Ex: 18000");
        dailyRateField.setText("18000");
        lengthField.setPromptText("Ex: 300");
        lengthField.setText("300");

        grid.add(createFieldBox("Código do Berço *", codeField), 0, 0);
        grid.add(createFieldBox("Nome / Terminal Arrendatário *", nameField), 1, 0);
        grid.add(createFieldBox("Região / Setor Cais *", sectorField), 2, 0);
        grid.add(createFieldBox("Calado Máximo (m) *", maxDraftField), 3, 0);

        grid.add(createFieldBox("Perfil de Carga APS (RN003) *", categoryCombo), 0, 1, 2, 1);
        grid.add(createFieldBox("Taxa Nominal Diária (t/dia — RN004) *", dailyRateField), 2, 1);
        grid.add(createFieldBox("Comprimento Acostável (m)", lengthField), 3, 1);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnSave = new Button("Salvar / Atualizar Berço");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> handleSaveBerth());

        Button btnClear = new Button("Limpar Formulário");
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

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Auditoria da Tabela de Berços Cadastrados — 60 Berços Comerciais de Santos (RF006)");
        tableTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField.setPromptText("Filtrar por código, terminal, setor ou perfil...");
        searchField.setPrefWidth(320);

        topBar.getChildren().addAll(tableTitle, spacer, searchField);

        FilteredList<Berth> filteredBerths = new FilteredList<>(controller.getBerths(), b -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.trim().toLowerCase();
            filteredBerths.setPredicate(b -> q.isEmpty()
                    || b.getCode().toLowerCase().contains(q)
                    || b.getName().toLowerCase().contains(q)
                    || b.getSector().toLowerCase().contains(q)
                    || b.getCargoCategory().getDisplayName().toLowerCase().contains(q));
        });

        TableColumn<Berth, String> colCode = new TableColumn<>("Código");
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        colCode.setPrefWidth(85);

        TableColumn<Berth, String> colName = new TableColumn<>("Nome / Terminal Portuário");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setPrefWidth(265);

        TableColumn<Berth, String> colSector = new TableColumn<>("Região / Setor APS");
        colSector.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSector()));
        colSector.setPrefWidth(210);

        TableColumn<Berth, String> colDraft = new TableColumn<>("Calado Máx");
        colDraft.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.of("pt", "BR"), "%.1f m", d.getValue().getMaxDraft())));
        colDraft.setPrefWidth(95);

        TableColumn<Berth, String> colCategory = new TableColumn<>("Perfil de Carga (APS)");
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCargoCategory().getDisplayName()));
        colCategory.setPrefWidth(215);

        TableColumn<Berth, String> colRate = new TableColumn<>("Taxa Diária (RN004)");
        colRate.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.of("pt", "BR"), "%,.0f t/dia", d.getValue().getNominalDailyRateTons())));
        colRate.setPrefWidth(130);

        TableColumn<Berth, Void> colActions = new TableColumn<>("Ações (Editar / RN024)");
        colActions.setPrefWidth(185);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Editar");
            private final Button btnDelete = new Button("Excluir");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("table-btn-edit");
                btnDelete.getStyleClass().add("table-btn-delete");

                btnEdit.setOnAction(e -> {
                    Berth b = getTableView().getItems().get(getIndex());
                    populateForm(b);
                });

                btnDelete.setOnAction(e -> {
                    Berth b = getTableView().getItems().get(getIndex());
                    int cascadeRemoved = controller.deleteBerthCascade(b);
                    showFeedback(String.format(
                            "RN024 — Berço %s excluído com sucesso (%d escalas associadas removidas em cascata).",
                            b.getCode(), cascadeRemoved), true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        berthTable.getColumns().setAll(colCode, colName, colSector, colDraft, colCategory, colRate, colActions);
        berthTable.setItems(filteredBerths);
        berthTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(berthTable, Priority.ALWAYS);

        berthTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });

        card.getChildren().addAll(topBar, berthTable);
        return card;
    }

    private VBox createFieldBox(String labelText, Control field) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void populateForm(Berth b) {
        codeField.setText(b.getCode());
        nameField.setText(b.getName());
        sectorField.setText(b.getSector());
        maxDraftField.setText(String.valueOf(b.getMaxDraft()));
        categoryCombo.setValue(b.getCargoCategory());
        dailyRateField.setText(String.format(Locale.US, "%.0f", b.getNominalDailyRateTons()));
        lengthField.setText(String.format(Locale.US, "%.0f", b.getLengthMeters()));
    }

    private void handleSaveBerth() {
        try {
            double draft = Double.parseDouble(maxDraftField.getText().trim().replace(",", "."));
            double rate = Double.parseDouble(dailyRateField.getText().trim().replace(",", "."));
            double length = Double.parseDouble(lengthField.getText().trim().replace(",", "."));

            Berth berth = new Berth(
                    codeField.getText(),
                    nameField.getText(),
                    sectorField.getText(),
                    draft,
                    categoryCombo.getValue(),
                    rate,
                    length
            );
            controller.saveOrUpdateBerth(berth);
            showFeedback("RN008 — Berço " + berth.getCode() + " salvo e sincronizado instantaneamente com o Lineup!", true);
            berthTable.refresh();
        } catch (NumberFormatException ex) {
            showFeedback("Erro de formato numérico: verifique os campos Calado Máximo, Taxa Diária e Comprimento.", false);
        } catch (IllegalArgumentException ex) {
            showFeedback(ex.getMessage(), false);
        }
    }

    private void clearForm() {
        codeField.clear();
        nameField.clear();
        sectorField.clear();
        maxDraftField.clear();
        categoryCombo.getSelectionModel().selectFirst();
        dailyRateField.setText("18000");
        lengthField.setText("300");
        berthTable.getSelectionModel().clearSelection();
        feedbackBanner.setText("");
        feedbackBanner.getStyleClass().setAll("feedback-banner-hidden");
    }

    private void showFeedback(String message, boolean success) {
        feedbackBanner.setText(message);
        feedbackBanner.getStyleClass().setAll(success ? "feedback-banner-success" : "feedback-banner-error");
    }

    private void updateTotalBadge() {
        totalCountBadge.setText("Total Cadastrado: " + controller.getBerths().size() + " Berços Comerciais");
    }
}
