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

class Estimation {
    private String modelName;
    private double size; // LOC or FP
    private double effort; // Person-months
    private double cost; // Monetary cost
    private double duration; // Time in months

    public Estimation(String modelName, double size, double effort, double cost, double duration) {
        this.modelName = modelName;
        this.size = size;
        this.effort = effort;
        this.cost = cost;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Model: " + modelName +
               ", Size: " + size +
               ", Effort: " + effort + " person-months" +
               ", Cost: $" + cost +
               ", Duration: " + duration + " months";
    }
}

public class EstimationApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/EstimationSystem", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveEstimationToDatabase(String projectName, Estimation estimation, String details) {
        String sql = "INSERT INTO Estimations (projectName, estimationModel, size, effort, cost, duration, inputDetails) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, projectName);
            pstmt.setString(2, estimation.modelName);
            pstmt.setDouble(3, estimation.size);
            pstmt.setDouble(4, estimation.effort);
            pstmt.setDouble(5, estimation.cost);
            pstmt.setDouble(6, estimation.duration);
            pstmt.setString(7, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Estimation calculateLOCBasedEstimation(double loc) {
        double effort = loc / 1000 * 2.4; // Example formula: Effort = LOC / 1000 * Coefficient
        double cost = effort * 5000; // Example cost per person-month
        double duration = effort / 4; // Example duration assuming 4 people per team
        return new Estimation("LOC-Based", loc, effort, cost, duration);
    }

    private Estimation calculateFunctionPointsEstimation(double fp) {
        double effort = fp * 1.5; // Example formula: Effort = FP * Coefficient
        double cost = effort * 5000; // Example cost per person-month
        double duration = effort / 3; // Example duration assuming 3 people per team
        return new Estimation("Function Points-Based", fp, effort, cost, duration);
    }

    private Estimation calculateCOCOMOEstimation(double loc) {
        double effort = 2.5 * Math.pow(loc / 1000, 1.05); // Basic COCOMO formula
        double cost = effort * 6000; // Example cost per person-month
        double duration = 2.5 * Math.pow(effort, 0.38); // Basic COCOMO formula
        return new Estimation("COCOMO-Based", loc, effort, cost, duration);
    }

    private void showEstimationChart(Stage primaryStage, Estimation estimation) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Metrics");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Values");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        BarChart.Series<String, Number> series = new BarChart.Series<>();
        series.setName(estimation.modelName);
        series.getData().add(new BarChart.Data<>("Effort", estimation.effort));
        series.getData().add(new BarChart.Data<>("Cost", estimation.cost));
        series.getData().add(new BarChart.Data<>("Duration", estimation.duration));

        barChart.getData().add(series);

        VBox vbox = new VBox(barChart);
        Scene scene = new Scene(vbox, 600, 400);
        Stage chartStage = new Stage();
        chartStage.setTitle("Estimation Metrics Chart");
        chartStage.setScene(scene);
        chartStage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField projectNameField = new TextField();
        projectNameField.setPromptText("Project Name");
        TextField sizeField = new TextField();
        sizeField.setPromptText("Size (LOC or FP)");
        ComboBox<String> modelComboBox = new ComboBox<>();
        modelComboBox.getItems().addAll("LOC-Based", "Function Points-Based", "COCOMO-Based");
        modelComboBox.setPromptText("Estimation Model");

        Button calculateButton = new Button("Calculate Estimation");
        Button chartButton = new Button("Show Metrics Chart");

        ListView<String> resultListView = new ListView<>();
        Estimation currentEstimation = null;

        calculateButton.setOnAction(e -> {
            String projectName = projectNameField.getText();
            double size = Double.parseDouble(sizeField.getText());
            String model = modelComboBox.getValue();
            String details = "Project: " + projectName + ", Model: " + model + ", Size: " + size;

            switch (model) {
                case "LOC-Based":
                    currentEstimation = calculateLOCBasedEstimation(size);
                    break;
                case "Function Points-Based":
                    currentEstimation = calculateFunctionPointsEstimation(size);
                    break;
                case "COCOMO-Based":
                    currentEstimation = calculateCOCOMOEstimation(size);
                    break;
                default:
                    resultListView.getItems().add("Invalid model selected.");
                    return;
            }

            resultListView.getItems().add(currentEstimation.toString());
            saveEstimationToDatabase(projectName, currentEstimation, details);
        });

        chartButton.setOnAction(e -> {
            if (currentEstimation != null) {
                showEstimationChart(primaryStage, currentEstimation);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Estimation Available");
                alert.setHeaderText("Calculate an estimation first!");
                alert.showAndWait();
            }
        });

        vbox.getChildren().addAll(projectNameField, sizeField, modelComboBox, calculateButton, chartButton, resultListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Software Estimation System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
