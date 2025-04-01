import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

class TestCase {
    private String testName;
    private String testType;
    private String inputData;
    private String expectedOutput;
    private String actualOutput;
    private String result;
    private int defects;

    public TestCase(String testName, String testType, String inputData, String expectedOutput, String actualOutput, String result, int defects) {
        this.testName = testName;
        this.testType = testType;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.result = result;
        this.defects = defects;
    }

    @Override
    public String toString() {
        return "Test Name: " + testName +
               ", Type: " + testType +
               ", Input Data: " + inputData +
               ", Expected Output: " + expectedOutput +
               ", Actual Output: " + actualOutput +
               ", Result: " + result +
               ", Defects Found: " + defects;
    }
}

class Defect {
    private String testName;
    private int severity;
    private String description;
    private String resolutionStatus;

    public Defect(String testName, int severity, String description, String resolutionStatus) {
        this.testName = testName;
        this.severity = severity;
        this.description = description;
        this.resolutionStatus = resolutionStatus;
    }

    @Override
    public String toString() {
        return "Test Name: " + testName +
               ", Severity: " + severity +
               ", Description: " + description +
               ", Resolution Status: " + resolutionStatus;
    }
}

public class TestingSystemApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/TestingSystem", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addTestCaseToDatabase(TestCase testCase) {
        String sql = "INSERT INTO TestCases (testName, testType, inputData, expectedOutput, actualOutput, result, defects) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, testCase.testName);
            pstmt.setString(2, testCase.testType);
            pstmt.setString(3, testCase.inputData);
            pstmt.setString(4, testCase.expectedOutput);
            pstmt.setString(5, testCase.actualOutput);
            pstmt.setString(6, testCase.result);
            pstmt.setInt(7, testCase.defects);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addDefectToDatabase(Defect defect) {
        String sql = "INSERT INTO Defects (testName, severity, description, resolutionStatus) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, defect.testName);
            pstmt.setInt(2, defect.severity);
            pstmt.setString(3, defect.description);
            pstmt.setString(4, defect.resolutionStatus);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<TestCase> getTestCasesFromDatabase() {
        List<TestCase> testCases = new ArrayList<>();
        String sql = "SELECT * FROM TestCases";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TestCase testCase = new TestCase(
                        rs.getString("testName"),
                        rs.getString("testType"),
                        rs.getString("inputData"),
                        rs.getString("expectedOutput"),
                        rs.getString("actualOutput"),
                        rs.getString("result"),
                        rs.getInt("defects")
                );
                testCases.add(testCase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return testCases;
    }

    private List<Defect> getDefectsFromDatabase() {
        List<Defect> defects = new ArrayList<>();
        String sql = "SELECT * FROM Defects";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Defect defect = new Defect(
                        rs.getString("testName"),
                        rs.getInt("severity"),
                        rs.getString("description"),
                        rs.getString("resolutionStatus")
                );
                defects.add(defect);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defects;
    }

    private void showDefectAnalysisChart(Stage primaryStage, List<Defect> defects) {
        PieChart pieChart = new PieChart();
        Map<String, Integer> severityCount = new HashMap<>();

        for (Defect defect : defects) {
            String severityLabel = "Severity " + defect.severity;
            severityCount.put(severityLabel, severityCount.getOrDefault(severityLabel, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : severityCount.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        VBox vbox = new VBox(pieChart);
        Scene scene = new Scene(vbox, 600, 400);
        Stage chartStage = new Stage();
        chartStage.setTitle("Defect Analysis Chart");
        chartStage.setScene(scene);
        chartStage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField testNameField = new TextField();
        testNameField.setPromptText("Test Name");
        TextField testTypeField = new TextField();
        testTypeField.setPromptText("Test Type (Unit, Integration, etc.)");
        TextField inputField = new TextField();
        inputField.setPromptText("Input Data");
        TextField expectedField = new TextField();
        expectedField.setPromptText("Expected Output");
        TextField actualField = new TextField();
        actualField.setPromptText("Actual Output");
        TextField defectsField = new TextField();
        defectsField.setPromptText("Number of Defects");

        Button addTestButton = new Button("Add Test Case");
        Button viewTestsButton = new Button("View Test Cases");
        Button viewDefectsButton = new Button("View Defects");
        Button showChartButton = new Button("Show Defect Analysis Chart");

        ListView<String> resultListView = new ListView<>();

        addTestButton.setOnAction(e -> {
            String testName = testNameField.getText();
            String testType = testTypeField.getText();
            String inputData = inputField.getText();
            String expectedOutput = expectedField.getText();
            String actualOutput = actualField.getText();
            String result = expectedOutput.equals(actualOutput) ? "Passed" : "Failed";
            int defects = Integer.parseInt(defectsField.getText());

            TestCase testCase = new TestCase(testName, testType, inputData, expectedOutput, actualOutput, result, defects);
            addTestCaseToDatabase(testCase);
            testNameField.clear();
            testTypeField.clear();
            inputField.clear();
            expectedField.clear();
            actualField.clear();
            defectsField.clear();
        });

        viewTestsButton.setOnAction(e -> {
            resultListView.getItems().clear();
            List<TestCase> testCases = getTestCasesFromDatabase();
        });

        viewDefectsButton.setOnAction(e -> {
            resultListView.getItems().clear();
            List<Defect> defects = getDefectsFromDatabase();
            for (Defect defect : defects) {
                resultListView.getItems().add(defect.toString());
            }
        });

        showChartButton.setOnAction(e -> {
            List<Defect> defects = getDefectsFromDatabase();
            showDefectAnalysisChart(primaryStage, defects);
        });

        vbox.getChildren().addAll(testNameField, testTypeField, inputField, expectedField, actualField, defectsField,
                                  addTestButton, viewTestsButton, viewDefectsButton, showChartButton, resultListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Testing System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
