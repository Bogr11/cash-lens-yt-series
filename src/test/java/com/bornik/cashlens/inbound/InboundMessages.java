package com.bornik.cashlens.inbound;

/**
 * Builds inbound test data from outside the package, where the entity is not visible.
 */
public final class InboundMessages {

    public static InboundMessageDto textMessage(String accountId, String externalId, String payload) {
        return new InboundMessageDto(InboundMessage.receivedAsText(accountId, externalId, payload));
    }

    private InboundMessages() {
    }

}
