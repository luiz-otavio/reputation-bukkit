SELECT true AS vote_exists FROM reputation_votes WHERE uuid = ? AND target_uuid = ? AND created_at >= ?;
