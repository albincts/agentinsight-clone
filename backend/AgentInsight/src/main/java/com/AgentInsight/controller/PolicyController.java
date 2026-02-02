package com.AgentInsight.controller;

import com.AgentInsight.dto.PolicyDto;
import com.AgentInsight.entity.Policy;
import com.AgentInsight.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/policies")
public class PolicyController {

    private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);

    @Autowired
    private PolicyService policyService;

    @GetMapping
    public ResponseEntity<List<PolicyDto>> getAllPolicies() {
        logger.info("Fetching all policies...");
        List<PolicyDto> policies = policyService.getAllPolicies();
        logger.info("Retrieved {} policies", policies.size());
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDto> getPolicyByPolicyId(@PathVariable String policyId) {
        logger.info("Fetching policy with ID: {}", policyId);
        PolicyDto policy = policyService.getPolicyByPolicyId(policyId);
        if (policy != null) {
            logger.info("Successfully retrieved policy {}", policyId);
            return ResponseEntity.ok(policy);
        }
        logger.warn("No policy found with ID: {}", policyId);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Policy> createPolicy(@RequestBody Policy policy) {
        logger.info("Creating new policy...");
        Policy created = policyService.createPolicy(policy);
        logger.info("Policy created successfully with ID: {}", created.getPolicyId());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<Policy> updatePolicy(@PathVariable String policyId, @RequestBody Policy policy) {
        logger.info("Updating policy with ID: {}", policyId);
        policy.setPolicyId(policyId);
        Policy updated = policyService.updatePolicy(policy);
        logger.info("Policy {} updated successfully", policyId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deletePolicy(@PathVariable String policyId) {
        logger.info("Deleting policy with ID: {}", policyId);
        policyService.deletePolicy(policyId);
        logger.info("Policy {} deleted successfully", policyId);
        return ResponseEntity.noContent().build();
    }
}
