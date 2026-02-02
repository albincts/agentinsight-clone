package com.AgentInsight.service.Impl;

import com.AgentInsight.dto.LeaderboardDto;
import com.AgentInsight.entity.Leaderboard;
import com.AgentInsight.entity.Users;
import com.AgentInsight.repository.LeaderboardRepository;
import com.AgentInsight.repository.SalesRepository;
import com.AgentInsight.repository.UserRepository;
import com.AgentInsight.service.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardServiceImpl.class);

    private final LeaderboardRepository leaderboardRepository;
    private final SalesRepository salesRepository;
    private final UserRepository userRepository;

    public LeaderboardServiceImpl(LeaderboardRepository leaderboardRepository,
                                  SalesRepository salesRepository,
                                  UserRepository userRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.salesRepository = salesRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshLeaderboard() {
        logger.info("Initiating leaderboard refresh process...");
        List<Object[]> results = salesRepository.findTotalSalesByAgent();

        if (results == null || results.isEmpty()) {
            logger.error("CRITICAL: Leaderboard refresh failed. No completed sales data found in the repository.");
            return;
        }

        try {
            logger.debug("Clearing existing leaderboard data...");
            leaderboardRepository.deleteAllInBatch();

            List<Leaderboard> newEntries = new ArrayList<>();
            int currentRank = 1;

            for (Object[] row : results) {
                String agentId = String.valueOf(row[0]);
                Double totalAmount = (row[1] instanceof Number) ? ((Number) row[1]).doubleValue() : 0.0;

                Users agent = userRepository.findById(agentId)
                        .orElseThrow(() -> {
                            logger.error("Refresh abort: Agent ID {} not found in user database.", agentId);
                            return new RuntimeException("Agent not found: " + agentId);
                        });

                Leaderboard lb = new Leaderboard();
                lb.setEntryId("LB-" + UUID.randomUUID().toString().substring(0, 8));
                lb.setAgent(agent);
                lb.setTotalSales(totalAmount);
                lb.setRank(currentRank++);
                lb.setLastUpdated(new Date());

                newEntries.add(lb);
                logger.trace("Ranked agent {}: Rank {}, Total Sales: {}", agentId, lb.getRank(), totalAmount);
            }

            leaderboardRepository.saveAllAndFlush(newEntries);
            logger.info("Leaderboard sync complete. Successfully stored {} new entries.", newEntries.size());

        } catch (Exception e) {
            logger.error("Unexpected error during leaderboard refresh: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<LeaderboardDto> getLeaderboard() {
        logger.debug("Requesting leaderboard data from database");
        List<LeaderboardDto> leaderboard = leaderboardRepository.findAllByOrderByRankAsc().stream()
                .map(l -> new LeaderboardDto(
                        l.getEntryId(),
                        l.getAgent() != null ? l.getAgent().getAgentid() : null,
                        l.getRank(),
                        l.getTotalSales(),
                        l.getAgent() != null ? l.getAgent().getName() : "Unknown Agent"
                ))
                .collect(Collectors.toList());
        logger.info("Retrieved {} leaderboard entries", leaderboard.size());
        return leaderboard;
    }

    @Override
    @Transactional
    public Leaderboard updateLeaderboardEntry(String entryId, Leaderboard updated) {
        logger.info("Manual update request for leaderboard entry ID: {}", entryId);
        Leaderboard lb = leaderboardRepository.findByEntryId(entryId)
                .orElseThrow(() -> {
                    logger.warn("Update failed: Entry ID {} not found", entryId);
                    return new RuntimeException("Entry not found: " + entryId);
                });

        lb.setRank(updated.getRank());
        lb.setTotalSales(updated.getTotalSales());
        lb.setLastUpdated(new Date());

        Leaderboard saved = leaderboardRepository.saveAndFlush(lb);
        logger.info("Entry ID {} updated successfully. New rank: {}", entryId, saved.getRank());
        return saved;
    }

    @Override
    @Transactional
    public void deleteLeaderboardEntry(String entryId) {
        logger.info("Request to delete leaderboard entry ID: {}", entryId);
        Leaderboard lb = leaderboardRepository.findByEntryId(entryId)
                .orElseThrow(() -> {
                    logger.error("Delete failed: Entry ID {} does not exist", entryId);
                    return new RuntimeException("Entry not found: " + entryId);
                });

        leaderboardRepository.delete(lb);
        leaderboardRepository.flush();
        logger.info("Entry ID {} successfully removed from leaderboard", entryId);
    }
}