INSERT INTO `reputation_votes` (
    `uuid`,
    `player_name`,
    `target_uuid`,
    `target_player_name`,
    `vote`,
    `amount`
) VALUES (?, ?, ?, ?, ?, ?);
