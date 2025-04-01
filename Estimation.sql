CREATE DATABASE EstimationSystem;

USE EstimationSystem;

CREATE TABLE Estimations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    projectName VARCHAR(100),
    estimationModel VARCHAR(50),
    size DOUBLE, -- LOC or FP
    effort DOUBLE,
    cost DOUBLE,
    duration DOUBLE,
    inputDetails TEXT
);
