package com.miguel.gamescollection.service;

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
    public List<Platform> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Platform findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("la plataforma", id));
    }

    @Transactional
    public Platform create(Platform platform) {
        return repository.save(platform);
    }

    @Transactional
    public Platform update(Integer id, Platform request) {
        Platform platform = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("la plataforma", id));

        platform.setName(request.getName());
        platform.setAbbreviation(request.getAbbreviation());
        platform.setManufacturer(request.getManufacturer());
        platform.setReleaseYear(request.getReleaseYear());

        return platform;
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("la plataforma", id);
        }
        repository.deleteById(id);
    }
}
