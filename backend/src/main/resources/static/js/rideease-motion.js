/**
 * RideEase Motion & Animation Utility System
 * Powered by GSAP & Leaflet smooth interpolations
 */
(function() {
    // 1. Toast Notification Slide-In Engine
    window.showRideToast = function(message, type = 'info', icon = 'bi-info-circle-fill') {
        let container = document.getElementById('rideeaseToastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'rideeaseToastContainer';
            container.className = 'rideeaseToastContainer rideease-toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = 'rideease-toast';
        
        const typeColors = {
            success: 'var(--driver-green)',
            warning: 'var(--accent-amber)',
            danger: 'var(--danger-red)',
            info: 'var(--primary-indigo)'
        };
        
        toast.style.borderLeftColor = typeColors[type] || typeColors.info;
        
        toast.innerHTML = `
            <i class="bi ${icon} fs-5" style="color: ${typeColors[type] || typeColors.info};"></i>
            <div>${message}</div>
        `;

        container.appendChild(toast);

        // Slide in
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // Auto dismiss after 3.5s
        setTimeout(() => {
            toast.classList.remove('show');
            toast.classList.add('hide');
            setTimeout(() => {
                if (toast.parentNode) toast.parentNode.removeChild(toast);
            }, 300);
        }, 3500);
    };

    // 2. Smooth Leaflet Marker Interpolator for Live GPS Telemetry
    window.animateMarkerTo = function(marker, targetLat, targetLon, durationMs = 800) {
        if (!marker) return;
        const startLatLng = marker.getLatLng();
        const startLat = startLatLng.lat;
        const startLng = startLatLng.lng;
        const startTime = performance.now();

        function easeOutCubic(t) {
            return 1 - Math.pow(1 - t, 3);
        }

        function step(now) {
            const elapsed = now - startTime;
            const progress = Math.min(elapsed / durationMs, 1);
            const eased = easeOutCubic(progress);

            const curLat = startLat + (targetLat - startLat) * eased;
            const curLng = startLng + (targetLon - startLng) * eased;

            marker.setLatLng([curLat, curLng]);

            if (progress < 1) {
                requestAnimationFrame(step);
            }
        }

        requestAnimationFrame(step);
    };

    // 3. Progressive Animated Route Drawing for Leaflet GeoJSON
    window.animateRoutePolyline = function(map, coordinates, options = {}) {
        if (!map || !coordinates || coordinates.length === 0) return null;

        // Leaflet expects [lat, lng], GeoJSON supplies [lng, lat]
        const latLngs = coordinates.map(c => [c[1], c[0]]);

        const polyline = L.polyline([], {
            color: options.color || '#4f46e5',
            weight: options.weight || 6,
            opacity: options.opacity || 0.9,
            lineJoin: 'round',
            lineCap: 'round',
            className: options.pulse ? 'animated-route-path' : ''
        }).addTo(map);

        let index = 0;
        const total = latLngs.length;
        const chunkSize = Math.max(1, Math.floor(total / 35));

        function drawNextSegment() {
            if (index < total) {
                const nextChunk = latLngs.slice(0, index + chunkSize);
                polyline.setLatLngs(nextChunk);
                index += chunkSize;
                requestAnimationFrame(drawNextSegment);
            } else {
                polyline.setLatLngs(latLngs);
            }
        }

        drawNextSegment();
        return polyline;
    };

    // 4. Interactive Movable & Expandable/Collapsible Sheet Handle Engine
    document.addEventListener('DOMContentLoaded', function() {
        const handleBars = document.querySelectorAll('.sheet-handle-bar');
        handleBars.forEach(handle => {
            const sheet = handle.closest('.floating-bottom-sheet, .tracking-sheet, .driver-hud-sheet');
            if (!sheet) return;

            // Click or tap on handle bar to cycle state: Default -> Expanded -> Minimized -> Default
            handle.addEventListener('click', function(e) {
                e.stopPropagation();
                if (sheet.classList.contains('sheet-expanded')) {
                    sheet.classList.remove('sheet-expanded');
                    sheet.classList.add('sheet-minimized');
                } else if (sheet.classList.contains('sheet-minimized')) {
                    sheet.classList.remove('sheet-minimized');
                } else {
                    sheet.classList.add('sheet-expanded');
                }
            });

            // Touch drag support for swiping up/down on mobile or touchscreens
            let startY = 0;
            let currentTranslate = 0;

            handle.addEventListener('touchstart', function(e) {
                if (e.touches.length === 1) {
                    startY = e.touches[0].clientY;
                }
            }, { passive: true });

            handle.addEventListener('touchend', function(e) {
                if (e.changedTouches.length === 1) {
                    const endY = e.changedTouches[0].clientY;
                    const diffY = endY - startY;

                    if (diffY < -40) {
                        // Swiped up -> Expand
                        sheet.classList.remove('sheet-minimized');
                        sheet.classList.add('sheet-expanded');
                    } else if (diffY > 40) {
                        // Swiped down -> Minimize or revert expanded
                        if (sheet.classList.contains('sheet-expanded')) {
                            sheet.classList.remove('sheet-expanded');
                        } else {
                            sheet.classList.add('sheet-minimized');
                        }
                    }
                }
            }, { passive: true });
        });
    });

})();
