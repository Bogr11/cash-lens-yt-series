package com.bornik.cashlens.inbound;

/**
 * Flat by design: the entity never leaves its package, and never travels to
 * another thread where its session is already closed.
 * <p>
 * For text the payload is the message itself and content is null; for a photo
 * the payload is the file name and content carries the image.
 */
public record InboundMessageDto(String payload, InputSource source, byte[] content) {

    static InboundMessageDto of(InboundMessage message) {
        return new InboundMessageDto(message.getPayload(), message.getSource(), message.getContent());
    }

}
