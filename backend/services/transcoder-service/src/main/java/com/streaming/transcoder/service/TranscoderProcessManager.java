package com.streaming.transcoder.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranscoderProcessManager {

    private final Map<UUID, Process> processes = new ConcurrentHashMap<>();

    public synchronized void start(UUID streamId, String streamKey) {

        if (processes.containsKey(streamId)) {
            return;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/transcode.sh"
            );

            processBuilder.environment().put("STREAM_ID", streamId.toString());
            processBuilder.environment().put("STREAM_KEY", streamKey);

            processBuilder.inheritIO();

            Process process = processBuilder.start();

            processes.put(streamId, process);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to start transcoder for stream " + streamId,
                    e
            );
        }
    }

    public synchronized void stop(UUID streamId) {

        Process process = processes.remove(streamId);

        if (process == null) {
            return;
        }

        process.destroy();

        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }

    public boolean isRunning(UUID streamId) {
        Process process = processes.get(streamId);
        return process != null && process.isAlive();
    }
}