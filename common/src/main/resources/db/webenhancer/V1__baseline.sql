-- Timestamp columns
ALTER TABLE ${logs} CHANGE `created` `created` BIGINT UNSIGNED;
ALTER TABLE ${playerPins} CHANGE `expires` `expires` BIGINT UNSIGNED;

-- Message hash column + index
ALTER TABLE ${logs} ADD COLUMN messageHash CHAR(64);
CREATE INDEX idx_${logs}_created_hash ON ${logs} (created, messageHash);

-- Backfill hashes (may fail on H2 which lacks SHA2 -- runner swallows the error)
UPDATE ${logs} SET messageHash = SHA2(message, 256) WHERE messageHash IS NULL;
