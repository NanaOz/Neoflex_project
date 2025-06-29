package com.javacode.deal.model.entity;

import com.javacode.deal.model.enums.ApplicationStatus;
import com.javacode.deal.model.jsonb.LoanOffer;
import com.javacode.deal.model.jsonb.StatusHistory;
import com.javacode.deal.model.jsonb.converter.JsonbConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "statement")
public class Statement {
    @Id
    @Column(name = "statement_id", nullable = false, updatable = false)
    @NotNull
    private UUID statementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    private Credit credit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private ApplicationStatus status;

    @Column(name = "creation_date", nullable = false)
    @NotNull
    private Timestamp creationDate;

    @Convert(converter = JsonbConverter.class)
    @Column(name = "applied_offer", columnDefinition = "jsonb")
    private LoanOffer appliedOffer;

    @Column(name = "sign_date")
    private Timestamp signDate;

    @Column(name = "ses_code")
    private String sesCode;

    @Convert(converter = JsonbConverter.class)
    @Column(name = "status_history", columnDefinition = "jsonb")
    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();

    public void addStatusHistory(StatusHistory history) {
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
        statusHistory.add(history);
    }
}
