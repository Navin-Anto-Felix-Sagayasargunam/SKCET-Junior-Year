import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class Risk {
    private String id;
    private String description;
    private double probability;
    private double impact;
    private String mitigationPlan;

    public Risk(String id, String description, double probability, double impact, String mitigationPlan) {
        this.id = id;
        this.description = description;
        this.probability = probability;
        this.impact = impact;
        this.mitigationPlan = mitigationPlan;
    }

    public String getId() {
        return id;
    }

    public double getRiskExposure() {
        return probability * impact;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Description: " + description +
                ", Probability: " + probability +
                ", Impact: $" + impact +
                ", Exposure: $" + getRiskExposure() +
                ", Mitigation: " + mitigationPlan;
    }
}

public class RiskManagementApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/RiskManagement", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addRiskToDatabase(String id, String description, double probability, double impact, String mitigation) {
        String sql = "INSERT INTO Risks (id, description, probability, impact, mitigationPlan) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, description);
            pstmt.setDouble(3, probability);
            pstmt.setDouble(4, impact);
            pstmt.setString(5, mitigation);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Risk> getAllRisksFromDatabase() {
        List<Risk> risks = new ArrayList<>();
        String sql = "SELECT * FROM Risks";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Risk risk = new Risk(
                        rs.getString("id"),
                        rs.getString("description"),
                        rs.getDouble("probability"),
                        rs.getDouble("impact"),
                        rs.getString("mitigationPlan")
                );
                risks.add(risk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return risks;
    }

    private void showPieChart(Stage primaryStage, List<Risk> risks) {
        PieChart pieChart = new PieChart();

        for (Risk risk : risks) {
            pieChart.getData().add(new PieChart.Data(risk.getId(), risk.getRiskExposure()));
        }

        VBox vbox = new VBox(pieChart);
        Scene scene = new Scene(vbox, 600, 400);
        Stage chartStage = new Stage();
        chartStage.setTitle("Risk Exposure Chart");
        chartStage.setScene(scene);
        chartStage.show();
    }

    private void checkHighExposure(List<Risk> risks) {
        for (Risk risk : risks) {
            if (risk.getRiskExposure() > 5000) { // Arbitrary threshold for high exposure
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("High Risk Alert");
                alert.setHeaderText("High Exposure Risk Detected!");
                alert.setContentText("Risk ID: " + risk.getId() + " has an exposure of $" + risk.getRiskExposure());
                alert.showAndWait();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField idField = new TextField();
        idField.setPromptText("Risk ID");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        TextField probField = new TextField();
        probField.setPromptText("Probability (0 to 1)");
        TextField impactField = new TextField();
        impactField.setPromptText("Impact (e.g., monetary value)");
        TextField mitField = new TextField();
        mitField.setPromptText("Mitigation Plan");

        Button addButton = new Button("Add Risk");
        Button viewButton = new Button("View Risks");
        Button chartButton = new Button("Show Risk Chart");

        ListView<String> riskListView = new ListView<>();

        addButton.setOnAction(e -> {
            String id = idField.getText();
            String desc = descField.getText();
            double prob = Double.parseDouble(probField.getText());
            double impact = Double.parseDouble(impactField.getText());
            String mit = mitField.getText();
            addRiskToDatabase(id, desc, prob, impact, mit);
            idField.clear();
            descField.clear();
            probField.clear();
            impactField.clear();
            mitField.clear();
        });

        viewButton.setOnAction(e -> {
            riskListView.getItems().clear();
            List<Risk> risks = getAllRisksFromDatabase();
            for (Risk risk : risks) {
                riskListView.getItems().add(risk.toString());
            }
        });

        chartButton.setOnAction(e -> {
            List<Risk> risks = getAllRisksFromDatabase();
            showPieChart(primaryStage, risks);
            checkHighExposure(risks);
        });

        vbox.getChildren().addAll(idField, descField, probField, impactField, mitField, addButton, viewButton, chartButton, riskListView);

        Scene scene = new Scene(vbox, 400, 500);
        primaryStage.setTitle("Risk Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
