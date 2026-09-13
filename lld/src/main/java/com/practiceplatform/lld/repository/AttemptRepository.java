package com.practiceplatform.lld.repository;

import com.practiceplatform.lld.model.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {}