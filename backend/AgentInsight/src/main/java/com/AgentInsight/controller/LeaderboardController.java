package com.AgentInsight.controller;

import com.AgentInsight.dto.LeaderboardDto;
import com.AgentInsight.entity.Leaderboard;
import com.AgentInsight.service.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/leaderboard")
@CrossOrigin(origins = "http://localhost:4200")
public class LeaderboardController {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public List<LeaderboardDto> getLeaderboard() {
        logger.info("REST request to fetch the current leaderboard data");
        List<LeaderboardDto> list = leaderboardService.getLeaderboard();
        logger.debug("Successfully retrieved {} entries from the leaderboard", list.size());
        return list;
    }

    @PostMapping("/refresh")
    public void refresh() {
        logger.info("Refreshing leaderboard data...");
        leaderboardService.refreshLeaderboard();
        logger.info("Leaderboard refresh completed");
    }

    @PutMapping("/{entryId}")
    public Leaderboard update(@PathVariable String entryId, @RequestBody Leaderboard lb) {
        logger.info("Updating leaderboard entry with id: {}", entryId);
        Leaderboard updated = leaderboardService.updateLeaderboardEntry(entryId, lb);
        logger.info("Leaderboard entry {} updated successfully", entryId);
        return updated;
    }

    @DeleteMapping("/{entryId}")
    public void delete(@PathVariable String entryId) {
        logger.info("Deleting leaderboard entry with id: {}", entryId);
        leaderboardService.deleteLeaderboardEntry(entryId);
        logger.info("Leaderboard entry {} deleted successfully", entryId);
    }
}
