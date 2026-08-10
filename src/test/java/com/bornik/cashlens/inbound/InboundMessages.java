package com.bornik.cashlens.inbound;

public final class InboundMessages {

    public static InboundMessageDto textMessage(String payload) {
        return new InboundMessageDto(payload, null, null, InputSource.TEXT_MESSAGE);
    }

    public static InboundMessageDto receipt(byte[] content, String contentType) {
        return new InboundMessageDto(null, content, contentType, InputSource.PHOTO);
    }

    public static InboundMessageDto voice(byte[] content, String contentType) {
        return new InboundMessageDto(null, content, contentType, InputSource.VOICE_MESSAGE);
    }

    private InboundMessages() {
    }

}
