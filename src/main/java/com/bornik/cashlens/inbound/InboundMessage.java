package com.bornik.cashlens.inbound;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@ToString
@Entity
@Table(name = "inbound_message")
@Getter
class InboundMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InputSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status = ProcessingStatus.RECEIVED;

    @Column(nullable = false, updatable = false)
    private final Instant receivedAt = Instant.now();

    InboundMessage(String payload, InputSource source) {
        this.payload = payload;
        this.source = source;
    }

}