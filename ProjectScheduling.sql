CREATE DATABASE ProjectScheduling;

USE ProjectScheduling;

CREATE TABLE Tasks (
    id VARCHAR(10) PRIMARY KEY,
    name TEXT,
    duration INT,
    dependencies TEXT,
    assignedResource TEXT,
    startDate DATE,
    endDate DATE
);

CREATE TABLE Resources (
    id VARCHAR(10) PRIMARY KEY,
    name TEXT,
    availability TEXT
);
