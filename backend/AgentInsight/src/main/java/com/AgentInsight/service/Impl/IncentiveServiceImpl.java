package com.AgentInsight.service.Impl;

import com.AgentInsight.CustomGenerator.IncentiveId.IncentiveIdGeneratorUtil;
import com.AgentInsight.dto.ResponceDTO.IncentiveResponseDTO;
import com.AgentInsight.dto.requestDTO.IncentiveRequestDTO;
import com.AgentInsight.entity.Incentive;
import com.AgentInsight.entity.Sales;
import com.AgentInsight.entity.Users;
import com.AgentInsight.repository.IncentiveRepository;
import com.AgentInsight.repository.UserRepository;
import com.AgentInsight.service.IncentiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncentiveServiceImpl implements IncentiveService {

    private static final Logger logger = LoggerFactory.getLogger(IncentiveServiceImpl.class);

    @Autowired
    private IncentiveRepository incentiveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncentiveIdGeneratorUtil incentiveIdGenerator;

    @Override
    public List<IncentiveResponseDTO> getIncentives() {
        logger.debug("Fetching all incentives");
        return incentiveRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IncentiveResponseDTO> getAllIncentivesWithDetails() {
        logger.debug("Fetching all incentives with full details and bonus calculations");
        return incentiveRepository.findAll().stream()
                .map(this::convertToDtoWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    public Page<IncentiveResponseDTO> getAllIncentivesWithDetails(Pageable pageable) {
        logger.debug("Fetching paginated incentives with details. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return incentiveRepository.findAll(pageable).map(this::convertToDtoWithDetails);
    }

    @Override
    public Page<IncentiveResponseDTO> getIncentives(Pageable pageable) {
        return getAllIncentivesWithDetails(pageable);
    }

    @Override
    public IncentiveResponseDTO getIncentiveByIncentiveId(String incentiveId) {
        logger.info("Fetching incentive details for ID: {}", incentiveId);
        return incentiveRepository.findByIncentiveid(incentiveId)
                .map(this::convertToDtoWithDetails)
                .orElseGet(() -> {
                    logger.warn("Incentive ID {} not found", incentiveId);
                    return null;
                });
    }

    @Override
    public List<IncentiveResponseDTO> getIncentivesByAgentId(String agentId) {
        logger.info("Fetching all incentives for agent ID: {}", agentId);
        return incentiveRepository.findByAgent_Agentid(agentId).stream()
                .map(this::convertToDtoWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalIncentivesAmount() {
        logger.debug("Calculating total global incentive amount");
        return getAllIncentivesWithDetails().stream()
                .mapToDouble(dto -> dto.getAmount() != null ? dto.getAmount() : 0.0)
                .sum();
    }

    @Override
    public Double getTotalBonusAmount() {
        logger.debug("Calculating total global bonus amount");
        return getAllIncentivesWithDetails().stream()
                .mapToDouble(dto -> dto.getBonus() != null ? dto.getBonus() : 0.0)
                .sum();
    }

    @Override
    public Integer getPendingCount() {
        logger.debug("Counting all pending incentives");
        return (int) getAllIncentivesWithDetails().stream()
                .filter(dto -> "pending".equalsIgnoreCase(dto.getStatus()))
                .count();
    }

    @Override
    @Transactional
    public void calculateAndCreateIncentiveForSale(Sales sale) {
        logger.info("Triggering incentive calculation for sale: {}", sale.getSaleid());
        String incentiveId = "I-" + sale.getSaleid();

        if (incentiveRepository.findByIncentiveid(incentiveId).isPresent()) {
            logger.warn("Incentive creation skipped: Record already exists for sale ID: {}", sale.getSaleid());
            return;
        }

        Double incentiveAmount = sale.getSaleamount() != null ? sale.getSaleamount() : 0.0;
        Users agent = sale.getAgent();

        Incentive incentive = new Incentive();
        incentive.setIncentiveid(incentiveId);
        incentive.setAgent(agent);
        incentive.setAmount(incentiveAmount);
        incentive.setCalculationdate(sale.getSaledate());
        incentive.setStatus("Pending");

        incentiveRepository.save(incentive);
        logger.info("Successfully created pending incentive: {} for agent: {}", incentiveId, (agent != null ? agent.getAgentid() : "N/A"));
    }

    @Override
    public List<IncentiveResponseDTO> getAllIncentivesWithDetailsForReports() {
        return getAllIncentivesWithDetails();
    }

    @Override
    public List<IncentiveResponseDTO> getAllIncentivesWithDetailsForPerformance() {
        return getAllIncentivesWithDetails();
    }

    @Override
    public IncentiveResponseDTO updateIncentiveStatus(String incentiveId, String status) {
        logger.info("Updating status for incentive ID: {} to {}", incentiveId, status);
        Incentive incentive = incentiveRepository.findByIncentiveid(incentiveId)
                .orElseThrow(() -> {
                    logger.error("Failed to update status: Incentive ID {} not found", incentiveId);
                    return new RuntimeException("Incentive not found: " + incentiveId);
                });

        String oldStatus = incentive.getStatus();
        incentive.setStatus(status);
        Incentive updated = incentiveRepository.save(incentive);
        logger.info("Incentive ID {} status changed from {} to {}", incentiveId, oldStatus, status);
        return convertToDtoWithDetails(updated);
    }

    @Override
    public IncentiveResponseDTO createIncentive(IncentiveRequestDTO incentiveRequest) {
        logger.info("Manual incentive creation requested for agent: {}", incentiveRequest.getAgentid());
        Users agent = userRepository.findById(incentiveRequest.getAgentid())
                .orElseThrow(() -> {
                    logger.error("Creation failed: Agent ID {} not found", incentiveRequest.getAgentid());
                    return new RuntimeException("Agent not found");
                });

        Incentive incentive = new Incentive();
        incentive.setAgent(agent);
        incentive.setAmount(incentiveRequest.getAmount());
        incentive.setCalculationdate(incentiveRequest.getCalculationdate());
        incentive.setStatus(incentiveRequest.getStatus());

        String generatedId = incentiveIdGenerator.generateId();
        incentive.setIncentiveid(generatedId);

        Incentive saved = incentiveRepository.save(incentive);
        logger.info("Manual incentive created with generated ID: {}", generatedId);
        return convertToDtoWithDetails(saved);
    }

    @Override
    public IncentiveResponseDTO convertToDto(Incentive incentive) {
        String agentName = incentive.getAgent() != null ? incentive.getAgent().getName() : "Unknown Agent";
        return new IncentiveResponseDTO(
                incentive.getIncentiveid(),
                incentive.getAgent() != null ? incentive.getAgent().getAgentid() : null,
                incentive.getAmount(),
                incentive.getCalculationdate(),
                incentive.getStatus(),
                agentName,
                0.0
        );
    }

    @Override
    public IncentiveResponseDTO convertToDtoWithDetails(Incentive incentive) {
        String agentName = incentive.getAgent() != null ? incentive.getAgent().getName() : "Unknown Agent";
        Double amount = incentive.getAmount() != null ? incentive.getAmount() : 0.0;
        // Calculation logic: 10% bonus
        Double bonus = Math.round((amount * 0.1) * 100.0) / 100.0;

        return new IncentiveResponseDTO(
                incentive.getIncentiveid(),
                incentive.getAgent() != null ? incentive.getAgent().getAgentid() : null,
                amount,
                incentive.getCalculationdate(),
                incentive.getStatus(),
                agentName,
                bonus
        );
    }
}