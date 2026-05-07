package com.bankofabyssinia.letter_serial_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.entity.District;

public interface BranchRepository extends JpaRepository<Branch, String> {

    @EntityGraph(attributePaths = { "district" })
    List<Branch> findByDistrict(District district);

    @EntityGraph(attributePaths = { "district" })
    Optional<Branch> findByDistrictAndIsDefaultTrue(District district);

    @EntityGraph(attributePaths = { "district" })
    @Override
    List<Branch> findAll();

    @EntityGraph(attributePaths = { "district" })
    @Override
    Optional<Branch> findById(String id);

    @Query("SELECT b FROM Branch b WHERE b.district.id = :districtId")
    @EntityGraph(attributePaths = { "district" })
    List<Branch> findByDistrictId(@Param("districtId") String districtId);

    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.district.id = :districtId AND LOWER(b.name) = LOWER(:name)")
    boolean existsByDistrictIdAndNameIgnoreCase(@Param("districtId") String districtId, @Param("name") String name);

    @Query("SELECT b FROM Branch b WHERE b.district.isActive = true")
    @EntityGraph(attributePaths = { "district" })
    List<Branch> findAllActiveBranches();

    @Query("SELECT b FROM Branch b WHERE b.district.code = :districtCode")
    @EntityGraph(attributePaths = { "district" })
    List<Branch> findByDistrictCode(@Param("districtCode") String districtCode);

    @Query("SELECT b FROM Branch b WHERE b.district.id = :districtId AND b.isDefault = true")
    @EntityGraph(attributePaths = { "district" })
    Optional<Branch> findDefaultByDistrictId(@Param("districtId") String districtId);

    // Add these new query methods:
    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.prefix = :prefix")
    boolean existsByPrefix(@Param("prefix") String prefix);

    @Query("SELECT COUNT(b) > 0 FROM Branch b WHERE b.prefix = :prefix AND b.id != :id")
    boolean existsByPrefixAndIdNot(@Param("prefix") String prefix, @Param("id") String id);

    @Query("SELECT COUNT(b) FROM Branch b WHERE b.district.id = :districtId")
    long countByDistrictId(@Param("districtId") String districtId);
}