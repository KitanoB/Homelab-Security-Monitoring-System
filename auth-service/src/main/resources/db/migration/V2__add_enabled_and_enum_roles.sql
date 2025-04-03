-- V2: Add 'enabled' column and convert 'role' to ENUM

-- Add 'enabled' column with default value
ALTER TABLE users
ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- Modify 'role' column to ENUM
ALTER TABLE users
MODIFY COLUMN role ENUM('USER', 'MANAGER', 'ADMIN') NOT NULL DEFAULT 'USER';