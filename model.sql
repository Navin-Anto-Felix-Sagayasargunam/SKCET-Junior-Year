CREATE DATABASE AnalysisModeling;

USE AnalysisModeling;

CREATE TABLE UseCases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(100),
    useCaseName VARCHAR(100),
    scenarioDescription TEXT
);

CREATE TABLE Processes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    processName VARCHAR(100),
    inputData TEXT,
    outputData TEXT
);

CREATE TABLE Classes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    className VARCHAR(100),
    attributes TEXT,
    operations TEXT
);

CREATE TABLE Relationships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    classA VARCHAR(100),
    classB VARCHAR(100),
    relationshipType VARCHAR(50) -- Example: Inheritance, Aggregation
);

CREATE TABLE Events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    eventName VARCHAR(100),
    triggeringCondition TEXT,
    responseAction TEXT
);
