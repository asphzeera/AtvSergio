package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.example.controller.PortController;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout Principal da Aplicação (MainLayout) com barra lateral escura (#0f172a)
 * e área de conteúdo clara (#f8fafc) permitindo navegação fluida entre as 6 telas do Modal Marítimo.
 */
public class MainLayout extends BorderPane {

    private final PortController controller;
    private final List<Button> navButtons = new ArrayList<>();

    private final LineupView lineupView;
    private final BerthManagementView berthManagementView;
    private final CargoCatalogView cargoCatalogView;
    private final VesselCatalogView vesselCatalogView;
    private final TideMonitoringView tideMonitoringView;
    private final ShipScheduleView shipScheduleView;

    public MainLayout(PortController controller) {
        this.controller = controller;
        getStyleClass().add("main-layout-root");

        this.lineupView = new LineupView(controller);
        this.berthManagementView = new BerthManagementView(controller);
        this.cargoCatalogView = new CargoCatalogView(controller);
        this.vesselCatalogView = new VesselCatalogView(controller);
        this.tideMonitoringView = new TideMonitoringView(controller);
        this.shipScheduleView = new ShipScheduleView(controller);

        setLeft(buildSidebar());
        setCenter(lineupView);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar-panel");
        sidebar.setPrefWidth(265);
        sidebar.setMinWidth(265);
        sidebar.setPadding(new Insets(20, 14, 20, 14));

        // Cabeçalho Institucional APS FIPS
        VBox brandBox = new VBox(4);
        brandBox.getStyleClass().add("sidebar-brand-box");
        brandBox.setPadding(new Insets(0, 6, 16, 6));

        Label badge = new Label("APS • FIPS • GÊMEO DIGITAL");
        badge.getStyleClass().add("sidebar-brand-badge");

        Label title = new Label("PORTO DE SANTOS");
        title.getStyleClass().add("sidebar-brand-title");

        Label subtitle = new Label("Modal Marítimo • RF001 a RF012");
        subtitle.getStyleClass().add("sidebar-brand-subtitle");

        brandBox.getChildren().addAll(badge, title, subtitle);

        Label menuSectionLbl = new Label("MÓDULOS OPERACIONAIS");
        menuSectionLbl.getStyleClass().add("sidebar-section-header");
        menuSectionLbl.setPadding(new Insets(8, 6, 4, 6));

        Button btnLineup = createNavButton("📊  Lineup Portuário Geral", "RF001 a RF004", () -> {
            lineupView.refreshAll();
            setCenter(lineupView);
        });
        Button btnBerths = createNavButton("⚓  Berços de Atracação (60)", "RF005 e RF006", () -> setCenter(berthManagementView));
        Button btnCargo = createNavButton("📦  Catálogo de Cargas APS", "RF007 (11 Perfis)", () -> setCenter(cargoCatalogView));
        Button btnVessels = createNavButton("🚢  Catálogo de Navios (IMO)", "RF008 e RF009", () -> setCenter(vesselCatalogView));
        Button btnTides = createNavButton("🌊  Tábuas de Marés (30m)", "RF010 (1.440 leituras)", () -> setCenter(tideMonitoringView));
        Button btnSchedule = createNavButton("📅  Programação de Chegadas", "RF011 e RF012", () -> setCenter(shipScheduleView));

        activateButton(btnLineup);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Card de Regras de Negócio Ativas no rodapé da barra lateral
        VBox rulesCard = new VBox(4);
        rulesCard.getStyleClass().add("sidebar-rules-card");
        Label rulesTitle = new Label("MOTOR DE REGRAS ATIVO");
        rulesTitle.getStyleClass().add("sidebar-rules-title");
        Label r1 = new Label("• RN001: Anti-Sobreposição");
        Label r2 = new Label("• RN002: Maré ≤ 0,4m / > 13,5m");
        Label r3 = new Label("• RN003: Perfil de Carga APS");
        Label r4 = new Label("• RN004: Duração Determinística");
        Label r5 = new Label("• RN007/008/024: IMO & Cascata");
        for (Label l : List.of(r1, r2, r3, r4, r5)) {
            l.getStyleClass().add("sidebar-rules-item");
        }
        rulesCard.getChildren().addAll(rulesTitle, r1, r2, r3, r4, r5);

        sidebar.getChildren().addAll(
                brandBox,
                menuSectionLbl,
                btnLineup,
                btnBerths,
                btnCargo,
                btnVessels,
                btnTides,
                btnSchedule,
                spacer,
                rulesCard
        );

        return sidebar;
    }

    private Button createNavButton(String mainText, String reqTag, Runnable action) {
        VBox content = new VBox(1);
        content.setAlignment(Pos.CENTER_LEFT);
        Label mainLbl = new Label(mainText);
        mainLbl.getStyleClass().add("nav-btn-title");
        Label subLbl = new Label(reqTag);
        subLbl.getStyleClass().add("nav-btn-sub");
        content.getChildren().addAll(mainLbl, subLbl);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("sidebar-nav-button");

        btn.setOnAction(e -> {
            activateButton(btn);
            action.run();
        });

        navButtons.add(btn);
        return btn;
    }

    private void activateButton(Button active) {
        for (Button b : navButtons) {
            b.getStyleClass().remove("sidebar-nav-button-active");
        }
        if (!active.getStyleClass().contains("sidebar-nav-button-active")) {
            active.getStyleClass().add("sidebar-nav-button-active");
        }
    }

    public Node getActiveView() {
        return getCenter();
    }
}
