package com.AgentInsight.service.Impl;

import com.AgentInsight.dto.PolicyDto;
import com.AgentInsight.entity.Policy;
import com.AgentInsight.repository.PolicyRepository;
import com.AgentInsight.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl implements PolicyService {

    private static final Logger logger = LoggerFactory.getLogger(PolicyServiceImpl.class);

    @Autowired
    private PolicyRepository policyRepository;

    @Override
    public List<PolicyDto> getAllPolicies() {
        logger.info("Fetching all policies");
        List<PolicyDto> policies = policyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.debug("Total policies retrieved: {}", policies.size());
        return policies;
    }

    @Override
    public PolicyDto getPolicyByPolicyId(String policyId) {
        logger.info("Fetching policy with ID: {}", policyId);
        Optional<Policy> policy = policyRepository.findByPolicyId(policyId);

        if (policy.isEmpty()) {
            logger.warn("Policy with ID {} not found", policyId);
        }

        return policy.map(this::convertToDto).orElse(null);
    }

    @Override
    public Policy createPolicy(Policy policy) {
        logger.info("Creating new policy: {}", policy.getName());
        Policy savedPolicy = policyRepository.save(policy);
        logger.info("Policy created successfully with ID: {}", savedPolicy.getPolicyId());
        return savedPolicy;
    }

    @Override
    public Policy updatePolicy(Policy policy) {
        logger.info("Updating policy with ID: {}", policy.getPolicyId());
        Policy updatedPolicy = policyRepository.save(policy);
        logger.info("Policy ID {} updated successfully", updatedPolicy.getPolicyId());
        return updatedPolicy;
    }

    @Override
    public void deletePolicy(String policyId) {
        logger.info("Deleting policy with ID: {}", policyId);
        try {
            policyRepository.deleteByPolicyId(policyId);
            logger.info("Policy ID {} deleted successfully", policyId);
        } catch (Exception e) {
            logger.error("Error occurred while deleting policy ID {}: {}", policyId, e.getMessage());
            throw e;
        }
    }

    private PolicyDto convertToDto(Policy policy) {
        return new PolicyDto(
                policy.getPolicyId(),
                policy.getName()
        );
    }
}