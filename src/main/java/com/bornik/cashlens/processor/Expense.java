package com.bornik.cashlens.processor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@ToString
@Entity
@Table(name = "expense")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String description;

    @Column
    private String merchant;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    Expense(String accountId, ParsedExpense parsed, String category) {
        this.accountId = accountId;
        this.amount = parsed.amount();
        this.currency = parsed.currency();
        this.category = category;
        this.description = parsed.description();
        this.merchant = parsed.merchant();
        this.confidence = parsed.confidence();
        this.createdDate = Instant.now();
    }

}
