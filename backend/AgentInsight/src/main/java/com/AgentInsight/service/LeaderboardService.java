package com.AgentInsight.service;

import com.AgentInsight.dto.LeaderboardDto;
import com.AgentInsight.entity.Leaderboard;
import java.util.List;

public interface LeaderboardService {
    List<LeaderboardDto> getLeaderboard();
    void refreshLeaderboard();
    Leaderboard updateLeaderboardEntry(String entryId, Leaderboard leaderboard);
    void deleteLeaderboardEntry(String entryId);
}