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

    /** The message itself for text, the original file name for a photo. */
    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    /**
     * The file itself — a photo or a voice note — stored in the same row and the
     * same transaction as the message. An inbox that keeps a record of a photo but
     * not the photo is not an inbox: the row would be a receipt for something we
     * no longer have.
     * <p>
     * Cleared once the message reaches PROCESSED: a phone photo is 3-5MB and the
     * parsed expense is what we actually keep. FAILED messages keep their bytes,
     * because those are the ones a retry would need.
     */
    @ToString.Exclude
    @Column(columnDefinition = "bytea")
    private byte[] content;

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
        return new InboundMessage(externalId, payload, source, null);
    }

    /** For carriers whose message is a file: the payload holds the name, the content holds the bytes. */
    static InboundMessage receivedFile(String externalId, String fileName, byte[] content, InputSource source) {
        return new InboundMessage(externalId, fileName, source, content);
    }

    void markProcessed() {
        this.status = ProcessingStatus.PROCESSED;
        this.content = null;
    }

    void markFailed() {
        this.status = ProcessingStatus.FAILED;
    }

    private InboundMessage(String externalId, String payload, InputSource source, byte[] content) {
        this.externalId = externalId;
        this.payload = payload;
        this.source = source;
        this.content = content;
        this.status = ProcessingStatus.RECEIVED;
        this.receivedAt = Instant.now();
    }

}
