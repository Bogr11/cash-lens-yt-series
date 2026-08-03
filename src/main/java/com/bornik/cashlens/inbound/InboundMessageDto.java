package com.bornik.cashlens.inbound;

public record InboundMessageDto(InboundMessage inboundMessage) {

    public String externalId() {
        return inboundMessage.getExternalId();
    }

    public String payload() {
        return inboundMessage.getPayload();
    }

}
