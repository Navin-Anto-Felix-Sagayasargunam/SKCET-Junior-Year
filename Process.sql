CREATE DATABASE ProcessModels;

USE ProcessModels;

CREATE TABLE ProcessPhases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    modelName VARCHAR(100),
    phaseName VARCHAR(100),
    progress DOUBLE, -- Percentage progress
    status VARCHAR(50) -- Completed/In Progress/Pending
);

CREATE TABLE Risks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    modelName VARCHAR(100),
    riskDescription TEXT,
    probability DOUBLE, -- Probability (0 to 1)
    impact DOUBLE -- Impact in monetary value or severity
);

CREATE TABLE AgileArtifacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sprintName VARCHAR(100),
    storyDescription TEXT,
    status VARCHAR(50) -- Completed/In Progress/Pending
);
