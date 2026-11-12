package org.example.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.controller.PortController;
import org.example.model.CargoCategory;
import org.example.model.RegisteredVessel;

import java.util.Locale;

/**
 * Tela de Cadastro, Consulta e Filtragem do Catálogo de Navios Mercantes (RF008, RF009 e RN007).
 */
public class VesselCatalogView extends BorderPane {

    private final PortController controller;

    private final TextField imoField = new TextField();
    private final TextField nameField = new TextField();
    private final ComboBox<String> vesselClassCombo = new ComboBox<>();
    private final TextField maxDraftField = new TextField();
    private final TextField dwtField = new TextField();
    private final ComboBox<CargoCategory> cargoCategoryCombo = new ComboBox<>();
    private final TextField flagField = new TextField();

    private final TextField searchField = new TextField();
    private final ComboBox<String> filterCategoryCombo = new ComboBox<>();
    private final Label feedbackBanner = new Label();
    private final Label fleetCountBadge = new Label();
    private final TableView<RegisteredVessel> vesselTable = new TableView<>();

    public VesselCatalogView(PortController controller) {
        this.controller = controller;
        getStyleClass().add("view-container");
        setPadding(new Insets(16, 20, 16, 20));

        VBox topSection = new VBox(12);
        topSection.getChildren().addAll(buildHeader(), buildFormCard());
        setTop(topSection);

        VBox centerSection = buildFleetTableCard();
        BorderPane.setMargin(centerSection, new Insets(12, 0, 0, 0));
        setCenter(centerSection);

        updateFleetBadge();
        controller.getRegisteredVessels().addListener((ListChangeListener<RegisteredVessel>) c -> updateFleetBadge());
    }

    private HBox buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label title = new Label("Catálogo e Gestão da Frota de Navios Mercantes (RF008 / RF009)");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Validação internacional de IMO (RN007), Calado Operacional Máximo (RN002) e Porte Bruto (DWT)");
        subtitle.getStyleClass().add("page-subtitle");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        fleetCountBadge.getStyleClass().add("info-pill-badge");
        header.getChildren().addAll(titles, spacer, fleetCountBadge);
        return header;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");

        Label formTitle = new Label("Cadastro / Atualização de Embarcação Mercante (RF008 — Validação RN007)");
        formTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        imoField.setPromptText("Ex: IMO-9845123 (Obrigatório IMO- + dígitos)");
        nameField.setPromptText("Ex: MV Atlantic Star");

        vesselClassCombo.getItems().setAll(
                "Capesize", "New Panamax", "Post-Panamax", "Panamax",
                "Supramax", "Handymax", "Suezmax", "Open Hatch Pulp",
                "Chemical Tanker", "Juice Tanker", "Ro-Ro Pure Car Carrier",
                "Cruise Liner", "Offshore Supply", "Feeder Container"
        );
        vesselClassCombo.setEditable(true);
        vesselClassCombo.getSelectionModel().select("Panamax");
        vesselClassCombo.setMaxWidth(Double.MAX_VALUE);

        maxDraftField.setPromptText("Ex: 13.8");
        dwtField.setPromptText("Ex: 78000");

        cargoCategoryCombo.getItems().setAll(CargoCategory.values());
        cargoCategoryCombo.getSelectionModel().selectFirst();
        cargoCategoryCombo.setMaxWidth(Double.MAX_VALUE);

        flagField.setPromptText("Ex: Panamá");
        flagField.setText("Panamá");

        grid.add(createFieldBox("Código Internacional IMO (RN007) *", imoField), 0, 0);
        grid.add(createFieldBox("Nome da Embarcação *", nameField), 1, 0);
        grid.add(createFieldBox("Classe Naval *", vesselClassCombo), 2, 0);
        grid.add(createFieldBox("Bandeira / Registro", flagField), 3, 0);

        grid.add(createFieldBox("Calado Operacional Máximo (m — RN002) *", maxDraftField), 0, 1);
        grid.add(createFieldBox("Porte Bruto / DWT (t) *", dwtField), 1, 1);
        grid.add(createFieldBox("Perfil de Carga Suportado (RN003) *", cargoCategoryCombo), 2, 1, 2, 1);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnSave = new Button("Cadastrar / Atualizar Navio");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> handleSaveVessel());

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
    private VBox buildFleetTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("table-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Consulta e Filtragem da Frota Cadastrada (RF009)");
        tableTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filterCategoryCombo.getItems().add("Todos os Perfis APS");
        for (CargoCategory c : CargoCategory.values()) {
            filterCategoryCombo.getItems().add(c.getDisplayName());
        }
        filterCategoryCombo.getSelectionModel().selectFirst();

        searchField.setPromptText("Buscar por IMO, Nome ou Classe Naval...");
        searchField.setPrefWidth(280);

        filterBar.getChildren().addAll(tableTitle, spacer, new Label("Perfil:"), filterCategoryCombo, searchField);

        FilteredList<RegisteredVessel> filteredVessels = new FilteredList<>(controller.getRegisteredVessels(), v -> true);
        Runnable applyFilter = () -> {
            String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String selectedCat = filterCategoryCombo.getValue();
            filteredVessels.setPredicate(v -> {
                boolean matchesText = q.isEmpty()
                        || v.getImoCode().toLowerCase().contains(q)
                        || v.getName().toLowerCase().contains(q)
                        || v.getVesselClass().toLowerCase().contains(q)
                        || v.getFlagCountry().toLowerCase().contains(q);
                boolean matchesCat = selectedCat == null
                        || selectedCat.equals("Todos os Perfis APS")
                        || v.getSupportedCargoCategory().getDisplayName().equals(selectedCat);
                return matchesText && matchesCat;
            });
        };
        searchField.textProperty().addListener((obs, o, n) -> applyFilter.run());
        filterCategoryCombo.valueProperty().addListener((obs, o, n) -> applyFilter.run());

        TableColumn<RegisteredVessel, String> colImo = new TableColumn<>("Código IMO (RN007)");
        colImo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getImoCode()));
        colImo.setPrefWidth(125);

        TableColumn<RegisteredVessel, String> colName = new TableColumn<>("Nome do Navio Mercante");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setPrefWidth(220);

        TableColumn<RegisteredVessel, String> colClass = new TableColumn<>("Classe Naval");
        colClass.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVesselClass()));
        colClass.setPrefWidth(145);

        TableColumn<RegisteredVessel, String> colDraft = new TableColumn<>("Calado Operacional");
        colDraft.setCellValueFactory(d -> {
            double draft = d.getValue().getMaxOperationalDraft();
            String tag = draft > 13.5 ? " (Restrito RN002)" : "";
            return new SimpleStringProperty(String.format(Locale.of("pt", "BR"), "%.1f m%s", draft, tag));
        });
        colDraft.setPrefWidth(165);

        TableColumn<RegisteredVessel, String> colDwt = new TableColumn<>("Porte Bruto (DWT)");
        colDwt.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.of("pt", "BR"), "%,.0f t", d.getValue().getDeadweightTonnage())));
        colDwt.setPrefWidth(130);

        TableColumn<RegisteredVessel, String> colCargo = new TableColumn<>("Perfil de Carga (RN003)");
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSupportedCargoCategory().getDisplayName()));
        colCargo.setPrefWidth(215);

        TableColumn<RegisteredVessel, String> colFlag = new TableColumn<>("Bandeira");
        colFlag.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFlagCountry()));
        colFlag.setPrefWidth(110);

        TableColumn<RegisteredVessel, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(145);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Editar");
            private final Button btnDelete = new Button("Excluir");
            private final HBox box = new HBox(6, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("table-btn-edit");
                btnDelete.getStyleClass().add("table-btn-delete");

                btnEdit.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> {
                    RegisteredVessel v = getTableView().getItems().get(getIndex());
                    controller.deleteVessel(v);
                    showFeedback("Navio " + v.getName() + " (" + v.getImoCode() + ") removido da frota.", true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        vesselTable.getColumns().setAll(colImo, colName, colClass, colDraft, colDwt, colCargo, colFlag, colActions);
        vesselTable.setItems(filteredVessels);
        vesselTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(vesselTable, Priority.ALWAYS);

        vesselTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) populateForm(newV);
        });

        card.getChildren().addAll(filterBar, vesselTable);
        return card;
    }

    private VBox createFieldBox(String labelText, Control control) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private void populateForm(RegisteredVessel v) {
        imoField.setText(v.getImoCode());
        nameField.setText(v.getName());
        vesselClassCombo.setValue(v.getVesselClass());
        maxDraftField.setText(String.valueOf(v.getMaxOperationalDraft()));
        dwtField.setText(String.format(Locale.US, "%.0f", v.getDeadweightTonnage()));
        cargoCategoryCombo.setValue(v.getSupportedCargoCategory());
        flagField.setText(v.getFlagCountry());
    }

    private void handleSaveVessel() {
        try {
            String imo = imoField.getText();
            if (!RegisteredVessel.isValidImo(imo)) {
                showFeedback("RN007 — Código IMO inválido! Deve iniciar obrigatoriamente com 'IMO-' seguido de números (ex: IMO-9812345).", false);
                return;
            }
            double draft = Double.parseDouble(maxDraftField.getText().trim().replace(",", "."));
            double dwt = Double.parseDouble(dwtField.getText().trim().replace(",", "."));

            RegisteredVessel vessel = new RegisteredVessel(
                    imo,
                    nameField.getText(),
                    vesselClassCombo.getEditor().getText().isBlank() ? vesselClassCombo.getValue() : vesselClassCombo.getEditor().getText(),
                    draft,
                    dwt,
                    cargoCategoryCombo.getValue(),
                    flagField.getText()
            );

            controller.saveOrUpdateVessel(vessel);
            vesselTable.refresh();
            showFeedback("Navio " + vessel.getName() + " (" + vessel.getImoCode() + ") salvo na frota com sucesso!", true);
        } catch (NumberFormatException ex) {
            showFeedback("Erro numérico: verifique os campos Calado Operacional (m) e Porte Bruto DWT (t).", false);
        } catch (IllegalArgumentException ex) {
            showFeedback(ex.getMessage(), false);
        }
    }

    private void clearForm() {
        imoField.clear();
        nameField.clear();
        vesselClassCombo.getSelectionModel().select("Panamax");
        maxDraftField.clear();
        dwtField.clear();
        cargoCategoryCombo.getSelectionModel().selectFirst();
        flagField.setText("Panamá");
        vesselTable.getSelectionModel().clearSelection();
        feedbackBanner.setText("");
        feedbackBanner.getStyleClass().setAll("feedback-banner-hidden");
    }

    private void showFeedback(String msg, boolean success) {
        feedbackBanner.setText(msg);
        feedbackBanner.getStyleClass().setAll(success ? "feedback-banner-success" : "feedback-banner-error");
    }

    private void updateFleetBadge() {
        fleetCountBadge.setText("Frota Cadastrada: " + controller.getRegisteredVessels().size() + " Navios Mercantes");
    }
}
