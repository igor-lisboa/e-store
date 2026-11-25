-- Preserve stable category IDs while bringing the platform label up to date.
UPDATE category
SET name = 'Nintendo'
WHERE id = 3 AND name = 'Wii';
