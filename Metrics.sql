CREATE DATABASE MetricsManagement;

USE MetricsManagement;

CREATE TABLE Metrics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    projectName VARCHAR(100),
    linesOfCode INT,
    cyclomaticComplexity INT,
    codeCoverage DOUBLE,
    defectDensity DOUBLE,
    defects INT,
    totalTests INT
);
