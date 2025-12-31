SELECT
    p.uuid AS prisoner_uuid,
    p.username,
    p.contraband,
    p.first_join,
    p.last_join,
    p.sentence,
    p.cell_uuid AS prisoner_cell_uuid,

    c.uuid AS cell_uuid,
    c.chunk_x,
    c.chunk_z
FROM `prisoner` p
JOIN cell c ON c.uuid = p.cell_uuid
WHERE p.uuid = ?