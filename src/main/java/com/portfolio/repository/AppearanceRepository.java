package com.portfolio.repository;

import com.portfolio.model.AppearanceSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppearanceRepository extends JpaRepository<AppearanceSetting, Long> {
}