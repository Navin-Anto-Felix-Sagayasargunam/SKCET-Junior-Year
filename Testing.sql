CREATE DATABASE TestingSystem;

USE TestingSystem;

CREATE TABLE TestCases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    testName VARCHAR(100),
    testType VARCHAR(50), -- Unit, Integration, etc.
    inputData TEXT,
    expectedOutput TEXT,
    actualOutput TEXT,
    result VARCHAR(20), -- Passed/Failed
    defects INT
);

CREATE TABLE Defects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    testName VARCHAR(100),
    severity INT, -- 1 = Low, 5 = Critical
    description TEXT,
    resolutionStatus VARCHAR(20) -- Open/Resolved
);
