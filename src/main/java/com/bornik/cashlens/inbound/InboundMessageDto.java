package com.bornik.cashlens.inbound;

public record InboundMessageDto(InboundMessage inboundMessage) {

    public String accountId() {
        return inboundMessage.getAccountId();
    }

    public String payload() {
        return inboundMessage.getPayload();
    }

    public InputSource source() {
        return inboundMessage.getSource();
    }

    public byte[] content() {
        return inboundMessage.getContent();
    }

    public String contentType() {
        return inboundMessage.getContentType();
    }

}
