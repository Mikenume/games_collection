package com.miguel.gamescollection.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "platforms")
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    @Column(nullable = false, length = 60, unique = true)
    private String name;

    @NotBlank(message = "La abreviatura es obligatoria")
    @Size(max = 10, message = "La abreviatura no puede superar los 10 caracteres")
    @Column(nullable = false, length = 10, unique = true)
    private String abbreviation;

    @NotBlank(message = "El fabricante es obligatorio")
    @Size(max = 60, message = "El fabricante no puede superar los 60 caracteres")
    @Column(nullable = false, length = 60)
    private String manufacturer;

    @Min(value = 1950, message = "El año debe ser 1950 o posterior")
    @Max(value = 2100, message = "El año debe ser 2100 o anterior")
    private Short releaseYear;

    public enum PlatformType { HOME, HANDHELD, HYBRID }

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PlatformType type;

    protected Platform() {
    }

    public Platform(String name, String abbreviation, String manufacturer, Short releaseYear) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.manufacturer = manufacturer;
        this.releaseYear = releaseYear;
    }

    public Integer getId() {

        return id;

    }
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getAbbreviation() {

        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {

        this.abbreviation = abbreviation;
    }

    public String getManufacturer() {

        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {

        this.manufacturer = manufacturer;
    }

    public Short getReleaseYear() {

        return releaseYear;
    }

    public void setReleaseYear(Short releaseYear) {

        this.releaseYear = releaseYear;
    }

    public PlatformType getType() {

        return type;
    }

    public void setType(PlatformType type) {

        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Platform other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
