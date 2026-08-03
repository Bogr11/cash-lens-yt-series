package com.bornik.cashlens.inbound;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@ToString
@Entity
@Table(name = "inbound_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class InboundMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InputSource source;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    static InboundMessage received(String externalId, String payload, InputSource source) {
        return new InboundMessage(externalId, payload, source);
    }

    private InboundMessage(String externalId, String payload, InputSource source) {
        this.externalId = externalId;
        this.payload = payload;
        this.source = source;
        this.status = ProcessingStatus.RECEIVED;
        this.receivedAt = Instant.now();
    }

}