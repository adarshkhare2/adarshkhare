package com.portfolio.repository;

import com.portfolio.model.Education;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findAllByOrderByEndYearDesc();
}
