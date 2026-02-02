package com.AgentInsight.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", unique = true)
    private String policyId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sales> sales;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Sales> getSales() { return sales; }
    public void setSales(List<Sales> sales) { this.sales = sales; }
}
