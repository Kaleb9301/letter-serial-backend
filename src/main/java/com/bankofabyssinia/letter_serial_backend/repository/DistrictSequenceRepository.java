package com.bankofabyssinia.letter_serial_backend.repository;

import com.bankofabyssinia.letter_serial_backend.entity.DistrictSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface DistrictSequenceRepository extends JpaRepository<DistrictSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DistrictSequence ds WHERE ds.id = :districtId")
    Optional<DistrictSequence> findByDistrictIdForUpdate(@Param("districtId") String districtId);

}