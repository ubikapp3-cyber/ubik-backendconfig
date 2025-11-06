package com.ubik.usermanagement.domain.port.in;

import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.model.RoomAvailability;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface ReservationUseCasePort {
    
    // Verificar disponibilidad
    Mono<RoomAvailability> checkRoomAvailability(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);
    
    Flux<Long> findAvailableRooms(Long motelId, LocalDateTime checkIn, LocalDateTime checkOut);
    
    // CRUD de reservas
    Mono<Reservation> createReservation(Reservation reservation);
    
    Mono<Reservation> getReservationById(Long id);
    
    Flux<Reservation> getReservationsByRoomId(Long roomId);
    
    Flux<Reservation> getReservationsByCustomerEmail(String email);
    
    Flux<Reservation> getActiveReservations(Long motelId);
    
    // Transiciones de estado
    Mono<Reservation> confirmReservation(Long id);
    
    Mono<Reservation> checkIn(Long id);
    
    Mono<Reservation> checkOut(Long id);
    
    Mono<Reservation> cancelReservation(Long id);
    
    // Gestión de bloqueos temporales
    Mono<Void> lockRoom(Long roomId, String sessionId, int minutesToLock);
    
    Mono<Void> unlockRoom(Long roomId, String sessionId);
}