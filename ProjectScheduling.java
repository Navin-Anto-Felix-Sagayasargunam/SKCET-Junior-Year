import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

class Task {
    private String id;
    private String name;
    private int duration; // Duration in days
    private List<String> dependencies;
    private String assignedResource;
    private LocalDate startDate;
    private LocalDate endDate;

    public Task(String id, String name, int duration, List<String> dependencies, String assignedResource) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.dependencies = dependencies;
        this.assignedResource = assignedResource;
        this.startDate = null;
        this.endDate = null;
    }

    public String getId() {
        return id;
    }

    public LocalDate calculateEndDate(LocalDate startDate) {
        this.startDate = startDate;
        this.endDate = startDate.plusDays(duration);
        return endDate;
    }

    @Override
    public String toString() {
        return "Task ID: " + id + ", Name: " + name +
               ", Duration: " + duration + " days, Dependencies: " + dependencies +
               ", Assigned Resource: " + assignedResource +
               ", Start Date: " + (startDate != null ? startDate : "Not Scheduled") +
               ", End Date: " + (endDate != null ? endDate : "Not Scheduled");
    }
}

public class ProjectSchedulingApp extends Application {

    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/ProjectScheduling", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addTaskToDatabase(String id, String name, int duration, String dependencies, String resource) {
        String sql = "INSERT INTO Tasks (id, name, duration, dependencies, assignedResource, startDate, endDate) VALUES (?, ?, ?, ?, ?, null, null)";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setInt(3, duration);
            pstmt.setString(4, dependencies);
            pstmt.setString(5, resource);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Task> getAllTasksFromDatabase() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM Tasks";
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task task = new Task(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("duration"),
                        Arrays.asList(rs.getString("dependencies").split(",")),
                        rs.getString("assignedResource")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private void scheduleTasks(List<Task> tasks) {
        Map<String, LocalDate> schedule = new HashMap<>();
        LocalDate projectStartDate = LocalDate.now();
        for (Task task : tasks) {
            LocalDate startDate = projectStartDate;
            for (String dependency : task.dependencies) {
                LocalDate dependencyEndDate = schedule.get(dependency);
                if (dependencyEndDate != null && dependencyEndDate.isAfter(startDate)) {
                    startDate = dependencyEndDate;
                }
            }
            LocalDate endDate = task.calculateEndDate(startDate);
            schedule.put(task.getId(), endDate);
        }
        System.out.println("--- Project Schedule ---");
        tasks.forEach(System.out::println);
    }

    private void showGanttChart(Stage primaryStage, List<Task> tasks) {
        CategoryAxis taskAxis = new CategoryAxis();
        taskAxis.setLabel("Tasks");

        NumberAxis timeAxis = new NumberAxis();
        timeAxis.setLabel("Timeline (Days)");

        GanttChart ganttChart = new GanttChart(taskAxis, timeAxis);

        for (Task task : tasks) {
            LocalDate startDate = task.startDate != null ? task.startDate : LocalDate.now();
            int startDays = (int) startDate.toEpochDay();
            int endDays = (int) task.endDate.toEpochDay();
            ganttChart.addData(task.getId(), startDays, endDays);
        }

        VBox vbox = new VBox(ganttChart);
        Scene scene = new Scene(vbox, 800, 600);
        Stage chartStage = new Stage();
        chartStage.setTitle("Project Schedule - Gantt Chart");
        chartStage.setScene(scene);
        chartStage.show();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        TextField idField = new TextField();
        idField.setPromptText("Task ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Task Name");
        TextField durationField = new TextField();
        durationField.setPromptText("Duration (Days)");
        TextField dependenciesField = new TextField();
        dependenciesField.setPromptText("Dependencies (Comma-separated Task IDs)");
        TextField resourceField = new TextField();
        resourceField.setPromptText("Assigned Resource");

        Button addTaskButton = new Button("Add Task");
        Button viewTasksButton = new Button("View Tasks");
        Button scheduleButton = new Button("Schedule Tasks");
        Button chartButton = new Button("Show Gantt Chart");

        ListView<String> taskListView = new ListView<>();

        addTaskButton.setOnAction(e -> {
            String id = idField.getText();
            String name = nameField.getText();
            int duration = Integer.parseInt(durationField.getText());
            String dependencies = dependenciesField.getText();
            String resource = resourceField.getText();
            addTaskToDatabase(id, name, duration, dependencies, resource);
            idField.clear();
            nameField.clear();
            durationField.clear();
            dependenciesField.clear();
            resourceField.clear();
        });

        viewTasksButton.setOnAction(e -> {
            taskListView.getItems().clear();
            List<Task> tasks = getAllTasksFromDatabase();
            for (Task task : tasks) {
                taskListView.getItems().add(task.toString());
            }
        });

        scheduleButton.setOnAction(e -> {
            List<Task> tasks = getAllTasksFromDatabase();
            scheduleTasks(tasks);
        });

        chartButton.setOnAction(e -> {
            List<Task> tasks = getAllTasksFromDatabase();
            showGanttChart(primaryStage, tasks);
        });

        vbox.getChildren().addAll(idField, nameField, durationField, dependenciesField, resourceField,
                                  addTaskButton, viewTasksButton, scheduleButton, chartButton, taskListView);

        Scene scene = new Scene(vbox, 400, 600);
        primaryStage.setTitle("Project Scheduling System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
