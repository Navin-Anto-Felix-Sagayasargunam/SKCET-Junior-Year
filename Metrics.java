import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class Metrics {
    private int loc; // Lines of Code
    private int cyclomaticComplexity;
    private double codeCoverage; // Percentage
    private double defectDensity; // Defects per 1000 LOC
    private int defects;
    private int totalTests;

    public Metrics(int loc, int cyclomaticComplexity, double codeCoverage, double defectDensity, int defects, int totalTests) {
        this.loc = loc;
        this.cyclomaticComplexity = cyclomaticComplexity;
        this.codeCoverage = codeCoverage;
        this.defectDensity = defectDensity;
        this.defects = defects;
        this.totalTests = totalTests;
    }

    @Override
    public String toString() {
        return "Lines of Code: " + loc +
               ", Cyclomatic Complexity: " + cyclomaticComplexity +
               ", Code Coverage: " + codeCoverage + "%" +
               ", Defect Density: " + defectDensity + " defects/1000 LOC" +
               ", Defects: " + defects +
               ", Total Tests: " + totalTests;
    }
}

public class MetricsManagementApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/MetricsManagement", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveMetricsToDatabase(String projectName, Metrics metrics) {
        String sql = "INSERT INTO Metrics (projectName, linesOfCode, cyclomaticComplexity, codeCoverage, defectDensity, defects, totalTests) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, projectName);
            pstmt.setInt(2, metrics.loc);
            pstmt.setInt(3, metrics.cyclomaticComplexity);
            pstmt.setDouble(4, metrics.codeCoverage);
            pstmt.setDouble(5, metrics.defectDensity);
            pstmt.setInt(6, metrics.defects);
            pstmt.setInt(7, metrics.totalTests);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Metrics calculateMetrics(int loc, int cyclomaticComplexity, int defects, int totalTests) {
        double defectDensity = (double) defects / loc * 1000; // Defects per 1000 LOC
        double codeCoverage = (double) totalTests / loc * 100; // Percentage coverage
        return new Metrics(loc, cyclomaticComplexity, codeCoverage, defectDensity, defects, totalTests);
    }

    private void showMetricsChart(Stage primaryStage, Metrics metrics) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Metrics");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Values");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        BarChart.Series<String, Number> series = new BarChart.Series<>();
        series.setName("Metrics Chart");
        series.getData().add(new BarChart.Data<>("LOC", metrics.loc));
        series.getData().add(new BarChart.Data<>("Cyclomatic Complexity", metrics.cyclomaticComplexity));
        series.getData().add(new BarChart.Data<>("Code Coverage", metrics.codeCoverage));
        series.getData().add(new BarChart.Data<>("Defect Density", metrics.defectDensity));

        barChart.getData().add(series);

        VBox vbox = new VBox(barChart);
        Scene scene = new Scene(vbox, 600, 400);
        Stage chartStage = new Stage();
        chartStage.setTitle("Software Metrics Chart");
        chartStage.setScene(scene);
        chartStage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField projectNameField = new TextField();
        projectNameField.setPromptText("Project Name");
        TextField locField = new TextField();
        locField.setPromptText("Lines of Code");
        TextField complexityField = new TextField();
        complexityField.setPromptText("Cyclomatic Complexity");
        TextField defectsField = new TextField();
        defectsField.setPromptText("Defects");
        TextField testsField = new TextField();
        testsField.setPromptText("Total Tests");

        Button calculateButton = new Button("Calculate Metrics");
        Button chartButton = new Button("Show Metrics Chart");

        ListView<String> resultListView = new ListView<>();
        Metrics currentMetrics = null;

        calculateButton.setOnAction(e -> {
            String projectName = projectNameField.getText();
            int loc = Integer.parseInt(locField.getText());
            int complexity = Integer.parseInt(complexityField.getText());
            int defects = Integer.parseInt(defectsField.getText());
            int totalTests = Integer.parseInt(testsField.getText());

            currentMetrics = calculateMetrics(loc, complexity, defects, totalTests);
            resultListView.getItems().add(currentMetrics.toString());
            saveMetricsToDatabase(projectName, currentMetrics);
        });

        chartButton.setOnAction(e -> {
            if (currentMetrics != null) {
                showMetricsChart(primaryStage, currentMetrics);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Metrics Available");
                alert.setHeaderText("Calculate metrics first!");
                alert.showAndWait();
            }
        });

        vbox.getChildren().addAll(projectNameField, locField, complexityField, defectsField, testsField,
                                  calculateButton, chartButton, resultListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Metrics Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
