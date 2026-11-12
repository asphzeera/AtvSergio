package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.controller.PortController;
import org.example.service.MockPortDataServiceImpl;
import org.example.service.PortDataService;
import org.example.view.MainLayout;

import java.net.URL;

/**
 * Classe principal da aplicação JavaFX — Gêmeo Digital do Porto de Santos (APS FIPS) - Checkpoint 1.
 * Execução direta via terminal:
 *   mvn clean javafx:run
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        PortDataService dataService = new MockPortDataServiceImpl();
        PortController controller = new PortController(dataService);
        MainLayout mainLayout = new MainLayout(controller);

        Scene scene = new Scene(mainLayout, 1440, 860);

        URL cssUrl = getClass().getResource("/styles/main.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Gêmeo Digital do Porto de Santos (APS FIPS) — Modal Marítimo (Checkpoint 1 POO)");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(740);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
