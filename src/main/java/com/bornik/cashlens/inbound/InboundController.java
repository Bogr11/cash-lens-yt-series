package com.bornik.cashlens.inbound;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/inbound")
@RequiredArgsConstructor
class InboundController {

    private static final String ACCOUNT = "X-Account-Id";

    private final InboundService service;

    @PostMapping("/save/text")
    ResponseEntity<?> saveText(@RequestHeader(ACCOUNT) String accountId, @RequestBody InboundTextMessageDto dto) {
        service.receiveAsText(accountId, dto.externalId(), dto.payload());
        return ResponseEntity.accepted().build();
    }

    record InboundTextMessageDto(String externalId, String payload) {
        InboundTextMessageDto {
            Objects.requireNonNull(externalId);
            Objects.requireNonNull(payload);
        }
    }

    @PostMapping(value = "/save/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> savePhoto(@RequestHeader(ACCOUNT) String accountId, @RequestPart String externalId, @RequestPart MultipartFile file) {
        service.receiveAsFile(accountId, externalId, bytes(file), file.getContentType(), InputSource.PHOTO);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/save/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> saveVoice(@RequestHeader(ACCOUNT) String accountId, @RequestPart String externalId, @RequestPart MultipartFile file) {
        service.receiveAsFile(accountId, externalId, bytes(file), file.getContentType(), InputSource.VOICE_MESSAGE);
        return ResponseEntity.accepted().build();
    }

    private byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Uploaded file is not readable", e);
        }
    }

}