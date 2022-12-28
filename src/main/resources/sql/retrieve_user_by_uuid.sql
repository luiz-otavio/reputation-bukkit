SELECT
    u.player_name AS player_name,
    u.last_like AS last_like,
    u.last_dislike AS last_dislike,
    u.created_at AS created_at,
    u.updated_at AS updated_at,
    SUM(v.vote = 'LIKE') AS likes,
    SUM(v.vote = 'DISLIKE') AS dislikes
FROM reputation_users u LEFT JOIN reputation_votes v ON u.uuid = v.target_uuid WHERE u.uuid = ? GROUP BY u.uuid;
