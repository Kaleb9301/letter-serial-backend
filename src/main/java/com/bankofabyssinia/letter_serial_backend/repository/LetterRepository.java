package com.bankofabyssinia.letter_serial_backend.repository;

import com.bankofabyssinia.letter_serial_backend.entity.Letter;
import com.bankofabyssinia.letter_serial_backend.entity.SerialNumber;
import com.bankofabyssinia.letter_serial_backend.enums.SerialNumberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    boolean existsByDistrict_IdAndReferenceNumberIgnoreCase(String districtId, String referenceNumber);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE " +
            "(:districtId IS NULL OR l.district.id = :districtId) AND " +
            "(:branchId IS NULL OR l.branchId = :branchId) AND " +
            "(:search IS NULL OR l.recipient LIKE %:search% OR l.subject LIKE %:search% OR l.writer LIKE %:search% OR l.referenceNumber LIKE %:search% OR str(l.serialNumber.serialNumber) LIKE %:search%) AND "
            +
            "(:status IS NULL OR (" +
            "   (:status = 'VOIDED' AND l.isVoided = true) OR " +
            "   (:status = 'USED' AND l.isVoided = false AND l.serialNumber.status = 'USED') OR " +
            "   (:status = 'AVAILABLE' AND l.isVoided = false AND l.serialNumber.status = 'AVAILABLE')" +
            ")) AND " +
            "(:startDate IS NULL OR l.letterDate >= :startDate) AND " +
            "(:endDate IS NULL OR l.letterDate <= :endDate) AND " +
            "(:writer IS NULL OR l.writer LIKE %:writer%) " +
            "ORDER BY l.createdAt DESC")
    Page<Letter> findByFilters(@Param("districtId") String districtId,
            @Param("branchId") String branchId,
            @Param("search") String search,
            @Param("status") SerialNumberStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("writer") String writer,
            Pageable pageable);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE " +
            "(:districtId IS NULL OR l.district.id = :districtId) AND " +
            "(:branchId IS NULL OR l.branchId = :branchId) AND " +
            "(:search IS NULL OR l.recipient LIKE %:search% OR l.subject LIKE %:search% OR l.writer LIKE %:search% OR l.referenceNumber LIKE %:search% OR str(l.serialNumber.serialNumber) LIKE %:search%) AND "
            +
            "(:status IS NULL OR (" +
            "   (:status = 'VOIDED' AND l.isVoided = true) OR " +
            "   (:status = 'USED' AND l.isVoided = false AND l.serialNumber.status = 'USED') OR " +
            "   (:status = 'AVAILABLE' AND l.isVoided = false AND l.serialNumber.status = 'AVAILABLE')" +
            ")) AND " +
            "(:startDate IS NULL OR l.letterDate >= :startDate) AND " +
            "(:endDate IS NULL OR l.letterDate <= :endDate) AND " +
            "(:writer IS NULL OR l.writer LIKE %:writer%) " +
            "ORDER BY l.createdAt DESC")
    List<Letter> findByFiltersList(@Param("districtId") String districtId,
            @Param("branchId") String branchId,
            @Param("search") String search,
            @Param("status") SerialNumberStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("writer") String writer);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE " +
            "(:includeVoided IS NULL OR l.isVoided = :includeVoided) AND " +
            "(:districtId IS NULL OR l.district.id = :districtId) AND " +
            "(:branchId IS NULL OR l.branchId = :branchId) AND " +
            "(:search IS NULL OR l.recipient LIKE %:search% OR l.subject LIKE %:search% OR l.writer LIKE %:search% OR l.referenceNumber LIKE %:search% OR str(l.serialNumber.serialNumber) LIKE %:search%) AND "
            +
            "(:status IS NULL OR l.serialNumber.status = :status) AND " +
            "(:startDate IS NULL OR l.letterDate >= :startDate) AND " +
            "(:endDate IS NULL OR l.letterDate <= :endDate) AND " +
            "(:writer IS NULL OR l.writer LIKE %:writer%) " +
            "ORDER BY l.createdAt DESC")
    Page<Letter> findByFiltersIncludingVoided(@Param("districtId") String districtId,
            @Param("branchId") String branchId,
            @Param("search") String search,
            @Param("status") SerialNumberStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("writer") String writer,
            @Param("includeVoided") Boolean includeVoided,
            Pageable pageable);

    @Query("SELECT MAX(l.serialNumber.serialNumber) FROM Letter l WHERE l.district.id = :districtId AND l.isVoided = false")
    Optional<Long> findMaxActiveSerialByDistrict(@Param("districtId") String districtId);

    @Query("SELECT MAX(l.serialNumber.serialNumber) FROM Letter l WHERE l.district.id = :districtId AND (:branchId IS NULL OR l.branchId = :branchId) AND l.isVoided = false")
    Optional<Long> findMaxActiveSerialByDistrictAndBranch(@Param("districtId") String districtId,
            @Param("branchId") String branchId);

    @Query("SELECT COUNT(l) > 0 FROM Letter l WHERE l.serialNumber = :serialNumber AND l.district.id = :districtId AND (:branchId IS NULL OR l.branchId = :branchId) AND l.isVoided = false")
    boolean existsBySerialNumberAndDistrictId(@Param("serialNumber") SerialNumber serialNumber,
            @Param("districtId") String districtId,
            @Param("branchId") String branchId);

    @Query("SELECT COUNT(l) > 0 FROM Letter l WHERE l.serialNumber = :serialNumber AND l.district.id = :districtId AND (:branchId IS NULL OR l.branchId = :branchId) AND l.isVoided = false")
    boolean existsBySerialNumberAndIsVoidedFalse(@Param("serialNumber") SerialNumber serialNumber,
            @Param("districtId") String districtId,
            @Param("branchId") String branchId);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE l.district.id = :districtId AND l.isVoided = false ORDER BY l.createdAt DESC LIMIT 1")
    Optional<Letter> findTopByDistrictIdOrderByCreatedAtDesc(@Param("districtId") String districtId);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE l.district.id = :districtId AND l.isVoided = true ORDER BY l.voidedAt DESC")
    List<Letter> findVoidedLettersByDistrict(@Param("districtId") String districtId);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE l.id = :id")
    Optional<Letter> findByIdIncludingVoided(@Param("id") Long id);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE l.district.id = :districtId AND l.isVoided = false ORDER BY l.serialNumber.serialNumber DESC LIMIT 1")
    Optional<Letter> findLatestNonVoidedLetterByDistrict(@Param("districtId") String districtId);

    @EntityGraph(attributePaths = { "district", "serialNumber" })
    @Query("SELECT l FROM Letter l WHERE l.serialNumber = :serialNumber AND l.isVoided = false")
    List<Letter> findBySerialNumberAndIsVoidedFalse(@Param("serialNumber") SerialNumber serialNumber);

    @Query("SELECT COUNT(l) FROM Letter l WHERE l.district.id = :districtId AND l.isVoided = false")
    Long countNonVoidedLettersByDistrict(@Param("districtId") String districtId);

    @SuppressWarnings("null")
    @Override
    @EntityGraph(attributePaths = { "district", "serialNumber" })
    Optional<Letter> findById(@SuppressWarnings("null") Long id);
}