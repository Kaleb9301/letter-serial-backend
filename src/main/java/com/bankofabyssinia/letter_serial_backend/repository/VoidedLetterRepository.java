package com.bankofabyssinia.letter_serial_backend.repository;

import com.bankofabyssinia.letter_serial_backend.entity.VoidedLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoidedLetterRepository extends JpaRepository<VoidedLetter, Long> {

       @Query("SELECT vl FROM VoidedLetter vl WHERE " +
                     "(:districtId IS NULL OR vl.district.id = :districtId) AND " +
                     "(:search IS NULL OR vl.recipient LIKE %:search% OR vl.subject LIKE %:search% OR vl.writer LIKE %:search%) AND "
                     +
                     "(:startDate IS NULL OR vl.letterDate >= :startDate) AND " +
                     "(:endDate IS NULL OR vl.letterDate <= :endDate) AND " +
                     "(:writer IS NULL OR vl.writer LIKE %:writer%) " +
                     "ORDER BY vl.voidedAt DESC")
       List<VoidedLetter> findByFilters(@Param("districtId") String districtId,
                     @Param("search") String search,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("writer") String writer);

       boolean existsByOriginalLetterId(Long originalLetterId);

}