package com.rideease.javafx.util;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Movable, Pannable & Zoomable OpenStreetMap Slippy Tile Engine for JavaFX Canvas.
 */
public class OsmTileRenderer {

    private static final int TILE_SIZE = 256;
    private static final Map<String, Image> TILE_CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService TILE_EXECUTOR = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "OSM-Tile-Loader");
        t.setDaemon(true);
        return t;
    });

    public static void renderOsmMap(
            GraphicsContext gc,
            double canvasWidth,
            double canvasHeight,
            double pLat, double pLon,
            double dLat, double dLon,
            String pickupName,
            String dropoffName,
            double distanceKm,
            String activePhase,
            double panOffsetX,
            double panOffsetY,
            int zoomOffset,
            Runnable onTileLoadedCallback
    ) {
        if (gc == null || canvasWidth <= 0 || canvasHeight <= 0) return;

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // Center calculation
        double centerLat = (pLat + dLat) / 2.0;
        double centerLon = (pLon + dLon) / 2.0;
        int baseZoom = calculateZoomLevel(pLat, pLon, dLat, dLon, canvasWidth, canvasHeight);
        int zoom = Math.max(8, Math.min(18, baseZoom + zoomOffset));

        // Center global pixel with Pan Offset applied
        double centerPixelX = lonToPixelX(centerLon, zoom) - panOffsetX;
        double centerPixelY = latToPixelY(centerLat, zoom) - panOffsetY;

        double originPixelX = centerPixelX - (canvasWidth / 2.0);
        double originPixelY = centerPixelY - (canvasHeight / 2.0);

        int minTileX = (int) Math.floor(originPixelX / TILE_SIZE);
        int maxTileX = (int) Math.floor((originPixelX + canvasWidth) / TILE_SIZE);
        int minTileY = (int) Math.floor(originPixelY / TILE_SIZE);
        int maxTileY = (int) Math.floor((originPixelY + canvasHeight) / TILE_SIZE);

        // 1. Base canvas fill
        gc.setFill(Color.web("#e2e8f0"));
        gc.fillRoundRect(0, 0, canvasWidth, canvasHeight, 10, 10);

        // 2. Render OpenStreetMap Tiles
        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                double drawX = (tx * TILE_SIZE) - originPixelX;
                double drawY = (ty * TILE_SIZE) - originPixelY;

                String tileKey = zoom + "/" + tx + "/" + ty;
                Image tileImg = TILE_CACHE.get(tileKey);

                if (tileImg != null) {
                    gc.drawImage(tileImg, drawX, drawY, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.web("#f1f5f9"));
                    gc.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.web("#cbd5e1"));
                    gc.setLineWidth(1.0);
                    gc.strokeRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                    loadTileAsync(zoom, tx, ty, onTileLoadedCallback);
                }
            }
        }

        // 3. Pixel positions for markers
        double pCanvasX = lonToPixelX(pLon, zoom) - originPixelX;
        double pCanvasY = latToPixelY(pLat, zoom) - originPixelY;
        double dCanvasX = lonToPixelX(dLon, zoom) - originPixelX;
        double dCanvasY = latToPixelY(dLat, zoom) - originPixelY;

        // 4. Draw Driving Route Polyline
        gc.setStroke(Color.web("#4338ca", 0.5));
        gc.setLineWidth(8.0);
        gc.beginPath();
        gc.moveTo(pCanvasX, pCanvasY);
        gc.bezierCurveTo(
                pCanvasX + (dCanvasX - pCanvasX) * 0.4, pCanvasY - 20,
                pCanvasX + (dCanvasX - pCanvasX) * 0.7, dCanvasY + 20,
                dCanvasX, dCanvasY
        );
        gc.stroke();

        gc.setStroke(Color.web("#4f46e5"));
        gc.setLineWidth(4.5);
        gc.beginPath();
        gc.moveTo(pCanvasX, pCanvasY);
        gc.bezierCurveTo(
                pCanvasX + (dCanvasX - pCanvasX) * 0.4, pCanvasY - 20,
                pCanvasX + (dCanvasX - pCanvasX) * 0.7, dCanvasY + 20,
                dCanvasX, dCanvasY
        );
        gc.stroke();

        // 5. Markers
        drawPin(gc, pCanvasX, pCanvasY, "#10b981", "P", pickupName, true);
        drawPin(gc, dCanvasX, dCanvasY, "#ef4444", "D", dropoffName, false);

        // 6. Live Vehicle Indicator
        double carRatio = "DESTINATION".equalsIgnoreCase(activePhase) ? 0.65 : 0.28;
        double carX = pCanvasX + (dCanvasX - pCanvasX) * carRatio;
        double carY = pCanvasY + (dCanvasY - pCanvasY) * carRatio;
        gc.setFill(Color.web("#22c55e"));
        gc.fillOval(carX - 14, carY - 14, 28, 28);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.0);
        gc.strokeOval(carX - 14, carY - 14, 28, 28);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", 14));
        gc.fillText("🚕", carX - 9, carY + 5);

        // 7. Interactive Controls HUD
        gc.setFill(Color.web("#0f172a", 0.85));
        gc.fillRoundRect(8, 8, 240, 24, 6, 6);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.fillText(String.format(Locale.US, "🗺️ OSM • %.1f km | Drag to Pan", distanceKm), 14, 24);
    }

    private static void drawPin(GraphicsContext gc, double x, double y, String colorWeb, String letter, String label, boolean isLeft) {
        gc.setFill(Color.web(colorWeb));
        gc.fillOval(x - 12, y - 12, 24, 24);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.0);
        gc.strokeOval(x - 12, y - 12, 24, 24);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.fillText(letter, x - 4, y + 4);

        if (label != null && !label.isEmpty()) {
            String shortName = label.length() > 20 ? label.substring(0, 18) + ".." : label;
            gc.setFill(Color.web("#0f172a", 0.88));
            double pillX = isLeft ? x + 15 : x - 110;
            gc.fillRoundRect(pillX, y - 10, 95, 20, 6, 6);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", FontWeight.BOLD, 9));
            gc.fillText(shortName, pillX + 6, y + 4);
        }
    }

    private static void loadTileAsync(int zoom, int x, int y, Runnable callback) {
        String key = zoom + "/" + x + "/" + y;
        TILE_EXECUTOR.submit(() -> {
            try {
                String tileUrl = String.format("https://tile.openstreetmap.org/%d/%d/%d.png", zoom, x, y);
                HttpURLConnection conn = (HttpURLConnection) new URL(tileUrl).openConnection();
                conn.setRequestProperty("User-Agent", "RideEase-CabBooking/1.0 (contact@rideease.com)");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    try (InputStream in = conn.getInputStream()) {
                        Image img = new Image(in);
                        if (!img.isError()) {
                            TILE_CACHE.put(key, img);
                            if (callback != null) {
                                Platform.runLater(callback);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    private static int calculateZoomLevel(double pLat, double pLon, double dLat, double dLon, double w, double h) {
        double maxDelta = Math.max(Math.abs(pLat - dLat), Math.abs(pLon - dLon));
        if (maxDelta > 2.0) return 8;
        if (maxDelta > 1.0) return 9;
        if (maxDelta > 0.4) return 11;
        if (maxDelta > 0.15) return 12;
        if (maxDelta > 0.05) return 13;
        return 14;
    }

    private static double lonToPixelX(double lon, int zoom) {
        return ((lon + 180.0) / 360.0 * (1 << zoom)) * TILE_SIZE;
    }

    private static double latToPixelY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * (1 << zoom)) * TILE_SIZE;
    }
}
