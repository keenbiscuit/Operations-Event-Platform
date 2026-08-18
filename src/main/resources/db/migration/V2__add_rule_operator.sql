-- V2__add_rule_operator.sql
-- Add the operator column to the rules table

ALTER TABLE rules ADD COLUMN operator VARCHAR(30) NOT NULL;