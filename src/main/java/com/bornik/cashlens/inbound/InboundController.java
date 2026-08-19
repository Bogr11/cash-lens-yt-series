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

    private final InboundService service;

    @PostMapping("/save/text")
    ResponseEntity<?> saveText(@RequestBody InboundTextMessageDto dto) {
        service.receiveAsText(dto.externalId(), dto.payload());
        return ResponseEntity.accepted().build();
    }

    record InboundTextMessageDto(String externalId, String payload) {
        InboundTextMessageDto {
            Objects.requireNonNull(externalId);
            Objects.requireNonNull(payload);
        }
    }

    @PostMapping(value = "/save/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> savePhoto(@RequestPart String externalId, @RequestPart MultipartFile file) {
        service.receiveAsFile(externalId, bytes(file), file.getContentType(), InputSource.PHOTO);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/save/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> saveVoice(@RequestPart String externalId, @RequestPart MultipartFile file) {
        service.receiveAsFile(externalId, bytes(file), file.getContentType(), InputSource.VOICE_MESSAGE);
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