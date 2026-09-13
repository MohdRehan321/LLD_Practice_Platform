package com.practiceplatform.lld.repository;

import com.practiceplatform.lld.model.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {}
