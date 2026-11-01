package com.streaming.transcoder.controller;

import com.streaming.transcoder.dto.StartTranscoderRequest;
import com.streaming.transcoder.dto.StopTranscoderRequest;
import com.streaming.transcoder.service.TranscoderProcessManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/transcoders")
@RequiredArgsConstructor
public class TranscoderController {

    private final TranscoderProcessManager transcoderProcessManager;

    @PostMapping("/start")
    public ResponseEntity<Void> start(
            @Valid @RequestBody StartTranscoderRequest request
    ) {
        transcoderProcessManager.start(
                request.streamId(),
                request.streamKey()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stop(
            @Valid @RequestBody StopTranscoderRequest request
    ) {
        transcoderProcessManager.stop(request.streamId());

        return ResponseEntity.ok().build();
    }
}