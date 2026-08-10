package com.bornik.cashlens.inbound;

public record InboundMessageDto(String payload, byte[] content, String contentType, InputSource source) {

    static InboundMessageDto of(InboundMessage message) {
        return new InboundMessageDto(
                message.getPayload(), message.getContent(), message.getContentType(), message.getSource());
    }

}
