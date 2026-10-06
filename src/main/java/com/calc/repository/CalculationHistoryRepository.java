package com.calc.repository;

import com.calc.model.CalculationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculationHistoryRepository extends JpaRepository<CalculationHistory, Long> {

    Page<CalculationHistory> findByExpressionContainingIgnoreCase(String keyword, Pageable pageable);
}
