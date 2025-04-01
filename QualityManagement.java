import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class Defect {
    private String id;
    private String description;
    private int severity; // 1 = Low, 5 = Critical
    private int priority; // 1 = Low, 5 = High
    private String status; // Open, Closed, In Progress
    private String module;

    public Defect(String id, String description, int severity, int priority, String status, String module) {
        this.id = id;
        this.description = description;
        this.severity = severity;
        this.priority = priority;
        this.status = status;
        this.module = module;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Description: " + description +
               ", Severity: " + severity + ", Priority: " + priority +
               ", Status: " + status + ", Module: " + module;
    }
}

public class QualityManagementApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/QualityManagement", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addDefectToDatabase(String id, String description, int severity, int priority, String status, String module) {
        String sql = "INSERT INTO Defects (id, description, severity, priority, status, module) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, description);
            pstmt.setInt(3, severity);
            pstmt.setInt(4, priority);
            pstmt.setString(5, status);
            pstmt.setString(6, module);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Defect> getAllDefectsFromDatabase() {
        List<Defect> defects = new ArrayList<>();
        String sql = "SELECT * FROM Defects";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Defect defect = new Defect(
                        rs.getString("id"),
                        rs.getString("description"),
                        rs.getInt("severity"),
                        rs.getInt("priority"),
                        rs.getString("status"),
                        rs.getString("module")
                );
                defects.add(defect);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defects;
    }

    private void calculateQualityMetrics(int totalDefects, int linesOfCode, int defectsRemoved, int testCases) {
        double defectDensity = (double) totalDefects / linesOfCode;
        double defectRemovalEfficiency = (double) defectsRemoved / totalDefects * 100;
        double testCoverage = (double) testCases / linesOfCode * 100;

        System.out.println("--- Quality Metrics ---");
        System.out.println("Defect Density: " + defectDensity + " defects/LOC");
        System.out.println("Defect Removal Efficiency: " + defectRemovalEfficiency + "%");
        System.out.println("Test Coverage: " + testCoverage + "%");
    }

    private void showDefectsPieChart(Stage primaryStage, List<Defect> defects) {
        PieChart pieChart = new PieChart();
        Map<String, Integer> defectStatusCount = new HashMap<>();

        for (Defect defect : defects) {
            defectStatusCount.put(defect.status, defectStatusCount.getOrDefault(defect.status, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : defectStatusCount.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        VBox vbox = new VBox(pieChart);
        Scene scene = new Scene(vbox, 600, 400);
        Stage chartStage = new Stage();
        chartStage.setTitle("Defect Status Distribution");
        chartStage.setScene(scene);
        chartStage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField idField = new TextField();
        idField.setPromptText("Defect ID");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        TextField severityField = new TextField();
        severityField.setPromptText("Severity (1 to 5)");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority (1 to 5)");
        TextField statusField = new TextField();
        statusField.setPromptText("Status (Open, Closed, In Progress)");
        TextField moduleField = new TextField();
        moduleField.setPromptText("Module");

        Button addButton = new Button("Add Defect");
        Button viewButton = new Button("View Defects");
        Button chartButton = new Button("Show Defect Chart");
        Button metricsButton = new Button("Calculate Metrics");

        ListView<String> defectListView = new ListView<>();

        addButton.setOnAction(e -> {
            String id = idField.getText();
            String desc = descField.getText();
            int severity = Integer.parseInt(severityField.getText());
            int priority = Integer.parseInt(priorityField.getText());
            String status = statusField.getText();
            String module = moduleField.getText();
            addDefectToDatabase(id, desc, severity, priority, status, module);
            idField.clear();
            descField.clear();
            severityField.clear();
            priorityField.clear();
            statusField.clear();
            moduleField.clear();
        });

        viewButton.setOnAction(e -> {
            defectListView.getItems().clear();
            List<Defect> defects = getAllDefectsFromDatabase();
            for (Defect defect : defects) {
                defectListView.getItems().add(defect.toString());
            }
        });

        chartButton.setOnAction(e -> {
            List<Defect> defects = getAllDefectsFromDatabase();
            showDefectsPieChart(primaryStage, defects);
        });

        metricsButton.setOnAction(e -> {
            calculateQualityMetrics(50, 5000, 45, 200); // Example values for metrics
        });

        vbox.getChildren().addAll(idField, descField, severityField, priorityField, statusField, moduleField,
                                  addButton, viewButton, chartButton, metricsButton, defectListView);

        Scene scene = new Scene(vbox, 400, 500);
        primaryStage.setTitle("Quality Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
