package com.portfolio.repository;

import com.portfolio.model.VisitStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitStatRepository extends JpaRepository<VisitStat, Long> {
}