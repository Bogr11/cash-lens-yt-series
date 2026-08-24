package com.bornik.cashlens.inbound;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@RestController
@RequestMapping("/inbound")
@RequiredArgsConstructor
class InboundController {

    private static final Function<String, URI> LOCATION = extId -> URI.create("/inbound/" + extId);

    private static final Map<AcceptResult, Function<String, ResponseEntity<?>>> RESULT_MAP = Map.of(
            AcceptResult.ACCEPTED, extId -> ResponseEntity.accepted().location(LOCATION.apply(extId)).build(),
            AcceptResult.DUPLICATE, extId -> ResponseEntity.ok().location(LOCATION.apply(extId)).build()
    );

    private final InboundService service;

    @PostMapping("/save/text")
    ResponseEntity<?> saveText(@RequestBody InboundTextMessageDto dto) {
        var accept = service.receiveAsText(dto.externalId(), dto.payload());
        return RESULT_MAP.get(accept).apply(dto.externalId());
    }

    record InboundTextMessageDto(String externalId, String payload) {
        InboundTextMessageDto {
            Objects.requireNonNull(externalId);
            Objects.requireNonNull(payload);
        }
    }

    @PostMapping(value = "/save/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> savePhoto(@RequestPart String externalId, @RequestPart MultipartFile file) {
        var accept = service.receiveAsFile(externalId, bytes(file), file.getContentType(), InputSource.PHOTO);
        return RESULT_MAP.get(accept).apply(externalId);
    }

    @PostMapping(value = "/save/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> saveVoice(@RequestPart String externalId, @RequestPart MultipartFile file) {
        var accept = service.receiveAsFile(externalId, bytes(file), file.getContentType(), InputSource.VOICE_MESSAGE);
        return RESULT_MAP.get(accept).apply(externalId);
    }

    @GetMapping("/{externalId}")
    ResponseEntity<InboundMessageView> getStatus(@PathVariable String externalId) {
        return service.findByExternalId(externalId)
                .map(InboundMessageView::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record InboundMessageView(String externalId, InputSource source, ProcessingStatus status,
                              Instant receivedAt, String failureReason) {
        static InboundMessageView of(InboundMessage message) {
            return new InboundMessageView(message.getExternalId(),
                    message.getSource(),
                    message.getStatus(),
                    message.getReceivedAt(),
                    message.getFailureReason());
        }
    }

    private byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Uploaded file is not readable", e);
        }
    }

}