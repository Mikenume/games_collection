package com.miguel.gamescollection.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "platforms")
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 60, unique = true)
    private String name;

    @Column(nullable = false, length = 10, unique = true)
    private String abbreviation;

    @Column(nullable = false, length = 60)
    private String manufacturer;

    private Short releaseYear;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Platform other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
