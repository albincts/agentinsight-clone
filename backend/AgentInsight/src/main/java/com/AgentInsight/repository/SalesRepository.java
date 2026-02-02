package com.AgentInsight.repository;

import com.AgentInsight.entity.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, String> {

    @Query("SELECT SUM(s.saleamount) FROM Sales s")
    Double getTotalSaleAmount();

    Sales findBySaleid(String saleid);

    @Query("SELECT s.agent.agentid, SUM(s.saleamount) FROM Sales s " +
            "WHERE UPPER(s.status) = 'COMPLETED' " +
            "GROUP BY s.agent.agentid " +
            "ORDER BY SUM(s.saleamount) DESC")
    List<Object[]> findTotalSalesByAgent();

    List<Sales> findByAgent_Agentid(String agentid);
}
