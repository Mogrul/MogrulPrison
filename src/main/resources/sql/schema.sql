CREATE TABLE IF NOT EXISTS `prisoner` (
    uuid TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    contraband INTEGER NOT NULL DEFAULT 0,
    first_join INTEGER NOT NULL,
    last_join INTEGER NOT NULL,
    sentence TEXT NOT NULL,
    cell_uuid TEXT,
    mine_uuid TEXT,

    FOREIGN KEY (cell_uuid) REFERENCES cell(uuid) ON DELETE RESTRICT
    FOREIGN KEY (mine_uuid) REFERENCES mine(uuid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `cell` (
    uuid TEXT PRIMARY KEY,
    chunk_x INTEGER NOT NULL,
    chunk_z INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `mine` (
    uuid TEXT PRIMARY KEY,
    chunk_x INTEGER NOT NULL,
    chunk_z INTEGER NOT NULL,
    is_claimed INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `schematic` (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    length INTEGER NOT NULL,
    created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `schematic_block` (
    schem_id TEXT NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    block_state TEXT NOT NULL,
    PRIMARY KEY (schem_id, x, y, z)
    FOREIGN KEY (schem_id) REFERENCES schematic(id) ON DELETE CASCADE
)