package com.bornik.cashlens.inbound;

/**
 * Builds inbound test data from outside the package, where the entity is not visible.
 */
public final class InboundMessages {

    public static InboundMessageDto textMessage(String payload) {
        return new InboundMessageDto(new InboundMessage(payload, InputSource.TEXT_MESSAGE));
    }

    private InboundMessages() {
    }

}
