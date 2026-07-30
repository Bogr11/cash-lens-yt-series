package com.bornik.cashlens.processor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "expense")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String currency;

    private String category;

    private String description;

    private Double confidence;

    @Column(nullable = false, updatable = false)
    private final Instant createdDate = Instant.now();

    Expense(ParsedExpense parsed) {
        this.amount = parsed.amount();
        this.currency = parsed.currency();
        this.category = parsed.category();
        this.description = parsed.description();
        this.confidence = parsed.confidence();
    }

}
