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

class Phase {
    private String modelName;
    private String phaseName;
    private double progress; // Percentage
    private String status;

    public Phase(String modelName, String phaseName, double progress, String status) {
        this.modelName = modelName;
        this.phaseName = phaseName;
        this.progress = progress;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Model: " + modelName + ", Phase: " + phaseName + ", Progress: " + progress + "%, Status: " + status;
    }
}

class Risk {
    private String modelName;
    private String description;
    private double probability;
    private double impact;

    public Risk(String modelName, String description, double probability, double impact) {
        this.modelName = modelName;
        this.description = description;
        this.probability = probability;
        this.impact = impact;
    }

    @Override
    public String toString() {
        return "Model: " + modelName + ", Risk: " + description +
               ", Probability: " + probability + ", Impact: " + impact;
    }
}

class AgileArtifact {
    private String sprintName;
    private String storyDescription;
    private String status;

    public AgileArtifact(String sprintName, String storyDescription, String status) {
        this.sprintName = sprintName;
        this.storyDescription = storyDescription;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Sprint: " + sprintName + ", Story: " + storyDescription + ", Status: " + status;
    }
}

public class ProcessModelsApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/ProcessModels", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addPhaseToDatabase(Phase phase) {
        String sql = "INSERT INTO ProcessPhases (modelName, phaseName, progress, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phase.modelName);
            pstmt.setString(2, phase.phaseName);
            pstmt.setDouble(3, phase.progress);
            pstmt.setString(4, phase.status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addRiskToDatabase(Risk risk) {
        String sql = "INSERT INTO Risks (modelName, riskDescription, probability, impact) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, risk.modelName);
            pstmt.setString(2, risk.description);
            pstmt.setDouble(3, risk.probability);
            pstmt.setDouble(4, risk.impact);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addAgileArtifactToDatabase(AgileArtifact artifact) {
        String sql = "INSERT INTO AgileArtifacts (sprintName, storyDescription, status) VALUES (?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, artifact.sprintName);
            pstmt.setString(2, artifact.storyDescription);
            pstmt.setString(3, artifact.status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Phase> getPhasesFromDatabase() {
        List<Phase> phases = new ArrayList<>();
        String sql = "SELECT * FROM ProcessPhases";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Phase phase = new Phase(
                        rs.getString("modelName"),
                        rs.getString("phaseName"),
                        rs.getDouble("progress"),
                        rs.getString("status")
                );
                phases.add(phase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return phases;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField modelNameField = new TextField();
        modelNameField.setPromptText("Model Name");
        TextField phaseNameField = new TextField();
        phaseNameField.setPromptText("Phase Name");
        TextField progressField = new TextField();
        progressField.setPromptText("Progress (%)");
        TextField statusField = new TextField();
        statusField.setPromptText("Status (Completed/In Progress/Pending)");

        Button addPhaseButton = new Button("Add Phase");
        Button viewPhasesButton = new Button("View Phases");

        ListView<String> resultListView = new ListView<>();

        addPhaseButton.setOnAction(e -> {
            String modelName = modelNameField.getText();
            String phaseName = phaseNameField.getText();
            double progress = Double.parseDouble(progressField.getText());
            String status = statusField.getText();

            Phase phase = new Phase(modelName, phaseName, progress, status);
            addPhaseToDatabase(phase);

            modelNameField.clear();
            phaseNameField.clear();
            progressField.clear();
            statusField.clear();
        });

        viewPhasesButton.setOnAction(e -> {
            resultListView.getItems().clear();
            List<Phase> phases = getPhasesFromDatabase();
            for (Phase phase : phases) {
                resultListView.getItems().add(phase.toString());
            }
        });

        vbox.getChildren().addAll(modelNameField, phaseNameField, progressField, statusField, addPhaseButton,
                                  viewPhasesButton, resultListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Process Models Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
