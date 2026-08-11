package com.miguel.gamescollection.service;

import com.miguel.gamescollection.dto.GenreDto;
import com.miguel.gamescollection.dto.GenreRequest;
import com.miguel.gamescollection.exception.ResourceNotFoundException;
import com.miguel.gamescollection.model.Genre;
import com.miguel.gamescollection.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository repository;

    public GenreService(GenreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<GenreDto> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public GenreDto findById(Integer id) {
        Genre genre = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("el género", id));
        return toDto(genre);
    }

    @Transactional
    public GenreDto create(GenreRequest request) {
        return toDto(repository.save(new Genre(request.name())));
    }

    @Transactional
    public GenreDto update(Integer id, GenreRequest request) {
        Genre genre = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("el género", id));
        genre.setName(request.name());
        return toDto(genre);
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("el género", id);
        }
        repository.deleteById(id);
    }

    private GenreDto toDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}
