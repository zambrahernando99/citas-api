-- HU-007 / PRD RF-05: los regímenes son un catálogo fijo, precargado y de solo lectura.
-- Seed idempotente: no duplica códigos ni nombres que ya existan (p. ej., creados antes vía CRUD administrativo).
INSERT INTO regime_catalog (code, name, active)
SELECT 'CONTRIBUTIVO', 'Contributivo', TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM regime_catalog WHERE code = 'CONTRIBUTIVO' OR LOWER(name) = 'contributivo');

INSERT INTO regime_catalog (code, name, active)
SELECT 'SUBSIDIADO', 'Subsidiado', TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM regime_catalog WHERE code = 'SUBSIDIADO' OR LOWER(name) = 'subsidiado');

INSERT INTO regime_catalog (code, name, active)
SELECT 'ESPECIAL', 'Especial', TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM regime_catalog WHERE code = 'ESPECIAL' OR LOWER(name) = 'especial');

INSERT INTO regime_catalog (code, name, active)
SELECT 'EXCEPCION', 'Excepción', TRUE FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM regime_catalog WHERE code = 'EXCEPCION' OR LOWER(name) IN ('excepción', 'excepcion'));
