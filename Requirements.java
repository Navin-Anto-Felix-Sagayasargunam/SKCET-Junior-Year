import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class Requirement {
    private int id;
    private String type;
    private String description;
    private int priority;
    private String status;

    public Requirement(String type, String description, int priority, String status) {
        this.type = type;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }

    public Requirement(int id, String type, String description, int priority, String status) {
        this(type, description, priority, status);
        this.id = id;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Type: " + type + ", Description: " + description +
               ", Priority: " + priority + ", Status: " + status;
    }
}

public class RequirementsEngineeringApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/RequirementsEngineering", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addRequirementToDatabase(Requirement requirement) {
        String sql = "INSERT INTO Requirements (type, description, priority, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requirement.type);
            pstmt.setString(2, requirement.description);
            pstmt.setInt(3, requirement.priority);
            pstmt.setString(4, requirement.status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Requirement> getAllRequirementsFromDatabase() {
        List<Requirement> requirements = new ArrayList<>();
        String sql = "SELECT * FROM Requirements";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Requirement requirement = new Requirement(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getInt("priority"),
                        rs.getString("status")
                );
                requirements.add(requirement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requirements;
    }

    private void validateRequirement(int requirementId, String validationStatus, String feedback) {
        String sql = "INSERT INTO ValidationReports (requirementId, validationStatus, feedback) VALUES (?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requirementId);
            pstmt.setString(2, validationStatus);
            pstmt.setString(3, feedback);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();

        TextField typeField = new TextField();
        typeField.setPromptText("Requirement Type (Functional/Non-Functional)");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Requirement Description");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority (1-10)");
        TextField statusField = new TextField();
        statusField.setPromptText("Status (Elicited, Analyzed, etc.)");

        Button addRequirementButton = new Button("Add Requirement");
        Button viewRequirementsButton = new Button("View Requirements");

        ListView<String> resultListView = new ListView<>();

        addRequirementButton.setOnAction(e -> {
            String type = typeField.getText();
            String description = descriptionField.getText();
            int priority = Integer.parseInt(priorityField.getText());
            String status = statusField.getText();

            Requirement requirement = new Requirement(type, description, priority, status);
            addRequirementToDatabase(requirement);

            typeField.clear();
            descriptionField.clear();
            priorityField.clear();
            statusField.clear();
        });

        viewRequirementsButton.setOnAction(e -> {
            resultListView.getItems().clear();
            List<Requirement> requirements = getAllRequirementsFromDatabase();
            for (Requirement req : requirements) {
                resultListView.getItems().add(req.toString());
            }
        });

        vbox.getChildren().addAll(typeField, descriptionField, priorityField, statusField,
                                  addRequirementButton, viewRequirementsButton, resultListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Requirements Engineering System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
