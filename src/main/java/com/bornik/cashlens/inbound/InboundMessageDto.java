package com.bornik.cashlens.inbound;

/**
 * Flat by design: the entity never leaves its package, and never travels to
 * another thread where its session is already closed.
 * <p>
 * For text the payload is the message itself; for a photo it is the path to
 * the stored file. Which one it is, the source decides.
 */
public record InboundMessageDto(String payload, InputSource source) {

    static InboundMessageDto of(InboundMessage message) {
        return new InboundMessageDto(message.getPayload(), message.getSource());
    }

}
