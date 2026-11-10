package com.rideease.javafx.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lightweight STOMP over WebSocket Client for RideEase JavaFX Application
 * Enables real-time event subscriptions:
 * - /topic/drivers/requests (Incoming ride alerts)
 * - /topic/ride/{id}/status (Live booking status transitions)
 * - /topic/admin/fleet/events (Fleet telemetry & verifications)
 */
public class StompWebSocketClient implements WebSocket.Listener {

    private static final String WS_URL = "ws://localhost:8080/ws-ride/websocket";
    private WebSocket webSocket;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Consumer<Map<String, Object>>> subscriptions = new ConcurrentHashMap<>();
    private final StringBuilder buffer = new StringBuilder();
    private boolean isConnected = false;

    public void connect() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(WS_URL), this)
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        sendStompConnect();
                    })
                    .exceptionally(ex -> {
                        System.err.println("WebSocket connection to " + WS_URL + " failed: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            System.err.println("Could not initialize WebSocket client: " + ex.getMessage());
        }
    }

    private void sendStompConnect() {
        String connectFrame = "CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000";
        if (webSocket != null) {
            webSocket.sendText(connectFrame, true);
        }
    }

    public void subscribe(String destination, Consumer<Map<String, Object>> handler) {
        subscriptions.put(destination, handler);
        if (isConnected && webSocket != null) {
            String subId = "sub-" + Math.abs(destination.hashCode());
            String subFrame = "SUBSCRIBE\nid:" + subId + "\ndestination:" + destination + "\n\n\u0000";
            webSocket.sendText(subFrame, true);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        buffer.append(data);
        if (last) {
            String fullMessage = buffer.toString();
            buffer.setLength(0);
            processStompFrame(fullMessage);
        }
        webSocket.request(1);
        return null;
    }

    private void processStompFrame(String frame) {
        if (frame.startsWith("CONNECTED")) {
            isConnected = true;
            // Re-subscribe all active destinations
            for (String destination : subscriptions.keySet()) {
                String subId = "sub-" + Math.abs(destination.hashCode());
                String subFrame = "SUBSCRIBE\nid:" + subId + "\ndestination:" + destination + "\n\n\u0000";
                webSocket.sendText(subFrame, true);
            }
        } else if (frame.startsWith("MESSAGE")) {
            // Parse STOMP Message Frame
            int bodyIndex = frame.indexOf("\n\n");
            if (bodyIndex != -1) {
                String headers = frame.substring(0, bodyIndex);
                String body = frame.substring(bodyIndex + 2);
                if (body.endsWith("\u0000")) {
                    body = body.substring(0, body.length() - 1);
                }

                String destination = null;
                for (String line : headers.split("\n")) {
                    if (line.startsWith("destination:")) {
                        destination = line.substring("destination:".length()).trim();
                        break;
                    }
                }

                if (destination != null) {
                    try {
                        Map<String, Object> payload = objectMapper.readValue(body, Map.class);
                        for (Map.Entry<String, Consumer<Map<String, Object>>> entry : subscriptions.entrySet()) {
                            if (destination.matches(entry.getKey().replace("{id}", ".*"))) {
                                Consumer<Map<String, Object>> callback = entry.getValue();
                                Platform.runLater(() -> callback.accept(payload));
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        this.isConnected = false;
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        this.isConnected = false;
    }
}
