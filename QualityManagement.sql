CREATE DATABASE QualityManagement;

USE QualityManagement;

CREATE TABLE Defects (
    id VARCHAR(10) PRIMARY KEY,
    description TEXT,
    severity INT,
    priority INT,
    status VARCHAR(20),
    module VARCHAR(50)
);

CREATE TABLE QualityMetrics (
    totalDefects INT,
    linesOfCode INT,
    defectsRemoved INT,
    testCases INT
);
