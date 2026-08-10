package com.bornik.cashlens.inbound;

public record InboundMessageDto(InboundMessage inboundMessage) {

    public String payload() {
        return inboundMessage.getPayload();
    }

    public InputSource source() {
        return inboundMessage.getSource();
    }

}
