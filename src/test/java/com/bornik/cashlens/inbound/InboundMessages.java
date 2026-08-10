package com.bornik.cashlens.inbound;

/**
 * Builds inbound test data from outside the package, where the entity is not visible.
 */
public final class InboundMessages {

    public static InboundMessageDto textMessage(String payload) {
        return new InboundMessageDto(payload, InputSource.TEXT_MESSAGE);
    }

    public static InboundMessageDto receipt(String path) {
        return new InboundMessageDto(path, InputSource.PHOTO);
    }

    private InboundMessages() {
    }

}
