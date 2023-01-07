CREATE TABLE IF NOT EXISTS `reputation_votes`(
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `uuid` CHAR(36) NOT NULL,
    `player_name` VARCHAR(16) NOT NULL,
    `target_uuid` CHAR(36) NOT NULL,
    `target_player_name` VARCHAR(16) NOT NULL,
    `vote` ENUM('LIKE', 'DISLIKE') NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;