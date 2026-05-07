package com.bankofabyssinia.letter_serial_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bankofabyssinia.letter_serial_backend.entity.District;

public interface DistrictRepository extends JpaRepository<District, String> {

    @EntityGraph(attributePaths = { "branches" })
    Optional<District> findByCode(String code);

    @EntityGraph(attributePaths = { "branches" })
    @Override
    List<District> findAll();

    @EntityGraph(attributePaths = { "branches" })
    @Override
    Optional<District> findById(String id);

    // Find active districts
    @Query("SELECT d FROM District d WHERE d.isActive = true")
    @EntityGraph(attributePaths = { "branches" })
    List<District> findAllActive();

    // Check if district exists by code
    boolean existsByCode(String code);

    // Check if district exists by code excluding current ID
    @Query("SELECT COUNT(d) > 0 FROM District d WHERE d.code = :code AND d.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") String id);
}