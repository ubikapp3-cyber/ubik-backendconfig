package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.port.in.ServiceUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.ServiceDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones CRUD de Service
 * Adaptador primario en arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/services")
@Tag(name = "Services", description = "API para gestión de servicios de habitaciones")
public class ServiceController {

    private final ServiceUseCasePort serviceUseCasePort;
    private final ServiceDtoMapper serviceDtoMapper;

    public ServiceController(ServiceUseCasePort serviceUseCasePort, ServiceDtoMapper serviceDtoMapper) {
        this.serviceUseCasePort = serviceUseCasePort;
        this.serviceDtoMapper = serviceDtoMapper;
    }

    /**
     * Crea un nuevo servicio
     * POST /api/services
     */
    @Operation(summary = "Crear un nuevo servicio", description = "Crea un nuevo servicio disponible para habitaciones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servicio creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request) {
        return Mono.just(request)
                .map(serviceDtoMapper::toDomain)
                .flatMap(serviceUseCasePort::createService)
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * Obtiene un servicio por ID
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public Mono<ServiceResponse> getServiceById(@PathVariable Long id) {
        return serviceUseCasePort.getServiceById(id)
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * Obtiene todos los servicios
     * GET /api/services
     */
    @GetMapping
    public Flux<ServiceResponse> getAllServices() {
        return serviceUseCasePort.getAllServices()
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * Obtiene un servicio por nombre
     * GET /api/services/name/{name}
     */
    @GetMapping("/name/{name}")
    public Mono<ServiceResponse> getServiceByName(@PathVariable String name) {
        return serviceUseCasePort.getServiceByName(name)
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * Actualiza un servicio existente
     * PUT /api/services/{id}
     */
    @PutMapping("/{id}")
    public Mono<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        return Mono.just(request)
                .map(serviceDtoMapper::toDomain)
                .flatMap(service -> serviceUseCasePort.updateService(id, service))
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * Elimina un servicio
     * DELETE /api/services/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteService(@PathVariable Long id) {
        return serviceUseCasePort.deleteService(id);
    }

    /**
     * Obtiene los IDs de servicios de una habitación
     * GET /api/services/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public Flux<Long> getServiceIdsByRoomId(@PathVariable Long roomId) {
        return serviceUseCasePort.getServiceIdsByRoomId(roomId);
    }

    /**
     * Asocia un servicio a una habitación
     * POST /api/services/room/{roomId}/service/{serviceId}
     */
    @Operation(summary = "Asociar servicio a habitación", description = "Asocia un servicio existente a una habitación")
    @ApiResponse(responseCode = "201", description = "Servicio asociado exitosamente")
    @PostMapping("/room/{roomId}/service/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addServiceToRoom(
            @Parameter(description = "ID de la habitación", required = true) @PathVariable Long roomId,
            @Parameter(description = "ID del servicio", required = true) @PathVariable Long serviceId) {
        return serviceUseCasePort.addServiceToRoom(roomId, serviceId);
    }

    /**
     * Elimina un servicio de una habitación
     * DELETE /api/services/room/{roomId}/service/{serviceId}
     */
    @DeleteMapping("/room/{roomId}/service/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeServiceFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long serviceId) {
        return serviceUseCasePort.removeServiceFromRoom(roomId, serviceId);
    }
}
