package com.portfolio.repository;

import com.portfolio.model.Certification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findAllByOrderByYearDesc();
}