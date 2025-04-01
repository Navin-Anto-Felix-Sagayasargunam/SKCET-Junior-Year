import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class UseCase {
    private String actor;
    private String useCaseName;
    private String scenarioDescription;

    public UseCase(String actor, String useCaseName, String scenarioDescription) {
        this.actor = actor;
        this.useCaseName = useCaseName;
        this.scenarioDescription = scenarioDescription;
    }

    @Override
    public String toString() {
        return "Actor: " + actor + ", Use Case: " + useCaseName + ", Scenario: " + scenarioDescription;
    }
}

class Process {
    private String name;
    private String inputData;
    private String outputData;

    public Process(String name, String inputData, String outputData) {
        this.name = name;
        this.inputData = inputData;
        this.outputData = outputData;
    }

    @Override
    public String toString() {
        return "Process Name: " + name + ", Input Data: " + inputData + ", Output Data: " + outputData;
    }
}

class ClassModel {
    private String name;
    private String attributes;
    private String operations;

    public ClassModel(String name, String attributes, String operations) {
        this.name = name;
        this.attributes = attributes;
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "Class Name: " + name + ", Attributes: " + attributes + ", Operations: " + operations;
    }
}

class Event {
    private String name;
    private String triggeringCondition;
    private String responseAction;

    public Event(String name, String triggeringCondition, String responseAction) {
        this.name = name;
        this.triggeringCondition = triggeringCondition;
        this.responseAction = responseAction;
    }

    @Override
    public String toString() {
        return "Event Name: " + name + ", Trigger: " + triggeringCondition + ", Response: " + responseAction;
    }
}

public class AnalysisModelingApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/AnalysisModeling", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addUseCaseToDatabase(UseCase useCase) {
        String sql = "INSERT INTO UseCases (actor, useCaseName, scenarioDescription) VALUES (?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, useCase.actor);
            pstmt.setString(2, useCase.useCaseName);
            pstmt.setString(3, useCase.scenarioDescription);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addProcessToDatabase(Process process) {
        String sql = "INSERT INTO Processes (processName, inputData, outputData) VALUES (?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, process.name);
            pstmt.setString(2, process.inputData);
            pstmt.setString(3, process.outputData);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addClassToDatabase(ClassModel classModel) {
        String sql = "INSERT INTO Classes (className, attributes, operations) VALUES (?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, classModel.name);
            pstmt.setString(2, classModel.attributes);
            pstmt.setString(3, classModel.operations);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addEventToDatabase(Event event) {
        String sql = "INSERT INTO Events (eventName, triggeringCondition, responseAction) VALUES (?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, event.name);
            pstmt.setString(2, event.triggeringCondition);
            pstmt.setString(3, event.responseAction);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> getAllUseCasesFromDatabase() {
        List<String> useCases = new ArrayList<>();
        String sql = "SELECT * FROM UseCases";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                useCases.add("Actor: " + rs.getString("actor") + ", Use Case: " + rs.getString("useCaseName") +
                             ", Scenario: " + rs.getString("scenarioDescription"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return useCases;
    }

    private List<String> getAllClassesFromDatabase() {
        List<String> classes = new ArrayList<>();
        String sql = "SELECT * FROM Classes";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                classes.add("Class: " + rs.getString("className") +
                            ", Attributes: " + rs.getString("attributes") +
                            ", Operations: " + rs.getString("operations"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField actorField = new TextField();
        actorField.setPromptText("Actor");
        TextField useCaseNameField = new TextField();
        useCaseNameField.setPromptText("Use Case Name");
        TextArea scenarioDescriptionField = new TextArea();
        scenarioDescriptionField.setPromptText("Scenario Description");

        Button addUseCaseButton = new Button("Add Use Case");
        Button viewUseCasesButton = new Button("View Use Cases");

        ListView<String> resultListView = new ListView<>();

        addUseCaseButton.setOnAction(e -> {
            String actor = actorField.getText();
            String useCaseName = useCaseNameField.getText();
            String scenarioDescription = scenarioDescriptionField.getText();

            UseCase useCase = new UseCase(actor, useCaseName, scenarioDescription);
            addUseCaseToDatabase(useCase);

            actorField.clear();
            useCaseNameField.clear();
            scenarioDescriptionField.clear();
        });

        viewUseCasesButton.setOnAction(e -> {
            resultListView.getItems().clear();
            resultListView.getItems().addAll(getAllUseCasesFromDatabase());
        });

        vbox.getChildren().addAll(actorField, useCaseNameField, scenarioDescriptionField, addUseCaseButton,
                                  viewUseCasesButton, resultListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Analysis Modeling System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
