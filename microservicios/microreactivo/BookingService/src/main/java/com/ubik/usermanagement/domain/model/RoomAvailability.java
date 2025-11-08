package com.ubik.usermanagement.domain.model;

import java.time.LocalDateTime;

public record RoomAvailability(
    Long roomId,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    boolean isAvailable,
    String reason // "available", "reserved", "maintenance", etc.
) {
    public static RoomAvailability available(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return new RoomAvailability(roomId, checkIn, checkOut, true, "available");
    }

    public static RoomAvailability unavailable(Long roomId, LocalDateTime checkIn, 
                                              LocalDateTime checkOut, String reason) {
        return new RoomAvailability(roomId, checkIn, checkOut, false, reason);
    }
}