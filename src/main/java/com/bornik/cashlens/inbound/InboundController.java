package com.bornik.cashlens.inbound;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

@RestController
@RequestMapping("/inbound")
@RequiredArgsConstructor
class InboundController {

    private final InboundService service;

    @PostMapping("/save/text")
    ResponseEntity<?> saveTextMessage(@RequestBody InboundTextMessageDto dto) {
        service.receive(dto.externalId(), dto.payload());
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/save/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> savePhoto(@RequestPart String externalId,
                                @RequestPart MultipartFile file) {
        return saveFile(externalId, file, InputSource.PHOTO);
    }

    @PostMapping(value = "/save/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> saveVoice(@RequestPart String externalId,
                                @RequestPart MultipartFile file) {
        return saveFile(externalId, file, InputSource.VOICE_MESSAGE);
    }

    private ResponseEntity<?> saveFile(String externalId, MultipartFile file, InputSource source) {
        Objects.requireNonNull(externalId);
        service.receiveFile(externalId, bytesOf(file), file.getContentType(), source);
        return ResponseEntity.accepted().build();
    }

    private byte[] bytesOf(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file", e);
        }
    }

    record InboundTextMessageDto(String externalId, String payload) {
        InboundTextMessageDto {
            Objects.requireNonNull(externalId);
            Objects.requireNonNull(payload);
        }
    }

}
