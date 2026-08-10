package com.bornik.cashlens.processor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double confidence;

    /** Nullable: a typed sentence rarely names the shop, a receipt almost always does. */
    @Column
    private String merchant;

    /** The date on the receipt, not the moment we processed it. Nullable for the same reason. */
    @Column
    private LocalDate occurredAt;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    Expense(ParsedExpense parsed) {
        this.amount = parsed.amount();
        this.currency = parsed.currency();
        this.category = parsed.category();
        this.description = parsed.description();
        this.merchant = parsed.merchant();
        this.occurredAt = parsed.occurredAt();
        this.confidence = parsed.confidence();
        this.createdDate = Instant.now();
    }

}
