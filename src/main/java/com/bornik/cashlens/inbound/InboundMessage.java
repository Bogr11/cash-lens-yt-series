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

    @Column(columnDefinition = "text")
    private String payload;

    @ToString.Exclude
    @Column(columnDefinition = "bytea")
    private byte[] content;

    @Column
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InputSource source;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    static InboundMessage receivedText(String externalId, String payload) {
        InboundMessage message = new InboundMessage(externalId, InputSource.TEXT_MESSAGE);
        message.payload = payload;
        return message;
    }

    static InboundMessage receivedFile(String externalId, byte[] content, String contentType, InputSource source) {
        InboundMessage message = new InboundMessage(externalId, source);
        message.content = content;
        message.contentType = contentType;
        return message;
    }

    void markProcessed() {
        this.status = ProcessingStatus.PROCESSED;
        this.content = null;
    }

    void markFailed() {
        this.status = ProcessingStatus.FAILED;
    }

    private InboundMessage(String externalId, InputSource source) {
        this.externalId = externalId;
        this.source = source;
        this.status = ProcessingStatus.RECEIVED;
        this.receivedAt = Instant.now();
    }

}
