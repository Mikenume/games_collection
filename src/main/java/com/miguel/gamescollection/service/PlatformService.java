package com.miguel.gamescollection.service;

import com.miguel.gamescollection.dto.PlatformDto;
import com.miguel.gamescollection.dto.PlatformRequest;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Platform;
import com.miguel.gamescollection.repository.PlatformRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlatformService {

    private final PlatformRepository repository;

    public PlatformService(PlatformRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PlatformDto> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformDto findById(Integer id) {
        Platform platform = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("la plataforma", id));
        return toDto(platform);
    }

    @Transactional
    public PlatformDto create(PlatformRequest request) {
        Platform platform = new Platform(
                request.name(),
                request.abbreviation(),
                request.manufacturer(),
                request.releaseYear()
        );
        return toDto(repository.save(platform));
    }

    @Transactional
    public PlatformDto update(Integer id, PlatformRequest request) {
        Platform platform = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("la plataforma", id));

        platform.setName(request.name());
        platform.setAbbreviation(request.abbreviation());
        platform.setManufacturer(request.manufacturer());
        platform.setReleaseYear(request.releaseYear());

        // No hace falta llamar a save(): dentro de la transacción, Hibernate
        // detecta el cambio sobre una entidad gestionada y lanza el UPDATE al
        // hacer commit. Es lo que se llama "dirty checking".
        return toDto(platform);
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("la plataforma", id);
        }
        // Si la plataforma tiene ediciones, el ON DELETE RESTRICT de PostgreSQL
        // rechaza el borrado y el GlobalExceptionHandler lo traduce a un 409.
        repository.deleteById(id);
    }

    private PlatformDto toDto(Platform platform) {
        return new PlatformDto(
                platform.getId(),
                platform.getName(),
                platform.getAbbreviation(),
                platform.getManufacturer(),
                platform.getReleaseYear()
        );
    }
}
