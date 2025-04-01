CREATE DATABASE RiskManagement;

USE RiskManagement;

CREATE TABLE Risks (
    id VARCHAR(10) PRIMARY KEY,
    description TEXT,
    probability DOUBLE,
    impact DOUBLE,
    mitigationPlan TEXT
);
