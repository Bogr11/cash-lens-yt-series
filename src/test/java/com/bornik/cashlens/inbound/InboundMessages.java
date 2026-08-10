package com.bornik.cashlens.inbound;

/**
 * Builds inbound test data from outside the package, where the entity is not visible.
 */
public final class InboundMessages {

    public static InboundMessageDto textMessage(String payload) {
        return new InboundMessageDto(payload, InputSource.TEXT_MESSAGE, null);
    }

    public static InboundMessageDto receipt(byte[] content, String fileName) {
        return new InboundMessageDto(fileName, InputSource.PHOTO, content);
    }

    private InboundMessages() {
    }

}
