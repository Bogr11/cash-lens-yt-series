package com.bornik.cashlens.inbound;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inbound")
@RequiredArgsConstructor
class InboundController {

    private final InboundService service;

    @PostMapping("/save/text")
    ResponseEntity<?> saveTextMessage(@RequestBody InboundTextMessageDto inboundTextMessageDto) {
        service.process(inboundTextMessageDto.payload());
        return ResponseEntity.accepted().build();
    }

    record InboundTextMessageDto(String payload) {

    }

}
