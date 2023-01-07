SELECT
    u.player_name AS player_name,
    u.last_like AS last_like,
    u.last_dislike AS last_dislike,
    u.created_at AS created_at,
    u.updated_at AS updated_at,
    SUM(CASE WHEN v.vote = 'LIKE' THEN v.amount ELSE 0 END) AS likes,
    SUM(CASE WHEN v.vote = 'DISLIKE' THEN v.amount ELSE 0 END) AS dislikes
FROM reputation_users u LEFT JOIN reputation_votes v ON u.uuid = v.target_uuid WHERE u.uuid = ? GROUP BY u.uuid;
