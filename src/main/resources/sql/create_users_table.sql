CREATE TABLE IF NOT EXISTS `reputation_users`(
    `uuid` CHAR(36) NOT NULL PRIMARY KEY,
    `player_name` VARCHAR(16) NOT NULL,
    `last_like` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_dislike` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
