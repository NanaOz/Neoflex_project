package com.javacode.deal.repository;

import com.javacode.deal.model.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditRepository extends JpaRepository<Credit, UUID> {
}
