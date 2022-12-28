SELECT true AS vote_exists FROM reputation_votes WHERE uuid = ? AND created_at >= ?;
