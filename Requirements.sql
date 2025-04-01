CREATE DATABASE RequirementsEngineering;

USE RequirementsEngineering;

CREATE TABLE Requirements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50), -- Functional or Non-Functional
    description TEXT,
    priority INT,
    status VARCHAR(20) -- Elicited, Analyzed, Specified, Validated
);

CREATE TABLE ValidationReports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    requirementId INT,
    validationStatus VARCHAR(50), -- Approved, Rejected
    feedback TEXT,
    FOREIGN KEY (requirementId) REFERENCES Requirements(id)
);
