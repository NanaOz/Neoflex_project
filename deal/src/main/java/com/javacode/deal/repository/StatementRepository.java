package com.javacode.deal.repository;

import com.javacode.deal.model.entity.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, UUID> {
}
