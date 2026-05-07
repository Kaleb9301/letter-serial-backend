package com.bankofabyssinia.letter_serial_backend.repository;

import com.bankofabyssinia.letter_serial_backend.entity.District;
import com.bankofabyssinia.letter_serial_backend.entity.Branch;
import com.bankofabyssinia.letter_serial_backend.entity.SerialNumber;
import com.bankofabyssinia.letter_serial_backend.enums.SerialNumberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {
        Optional<SerialNumber> findByDistrictAndSerialNumber(District district, Long serialNumber);

        // Branch-aware lookup
        Optional<SerialNumber> findByDistrictAndBranchAndSerialNumber(District district, Branch branch,
                        Long serialNumber);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM SerialNumber s WHERE s.district.id = :districtId ORDER BY s.serialNumber DESC")
        Optional<SerialNumber> findTopByDistrictIdOrderBySerialNumberDesc(String districtId);

        @Query("SELECT s FROM SerialNumber s WHERE s.district.id = :districtId AND s.status = :status ORDER BY s.serialNumber DESC")
        List<SerialNumber> findTopByDistrictIdAndStatusOrderBySerialNumberDesc(String districtId,
                        SerialNumberStatus status);

        @Query("SELECT s FROM SerialNumber s WHERE s.district.id = :districtId AND s.status = :status ORDER BY s.serialNumber DESC")
        List<SerialNumber> findByDistrictIdAndStatusOrderBySerialNumberDesc(String districtId,
                        SerialNumberStatus status);

        // Branch-aware variant
        @Query("SELECT s FROM SerialNumber s WHERE s.district.id = :districtId AND (:branchId IS NULL OR s.branch.id = :branchId) AND s.status = :status ORDER BY s.serialNumber DESC")
        List<SerialNumber> findByDistrictIdAndBranchIdAndStatusOrderBySerialNumberDesc(
                        @Param("districtId") String districtId, @Param("branchId") String branchId,
                        SerialNumberStatus status);

        @Query("SELECT MAX(s.serialNumber) FROM SerialNumber s WHERE s.district.id = :districtId")
        Optional<Long> findMaxSerialNumberByDistrictId(String districtId);

        @Query("SELECT MAX(s.serialNumber) FROM SerialNumber s WHERE s.district.id = :districtId AND (:branchId IS NULL OR s.branch.id = :branchId)")
        Optional<Long> findMaxSerialNumberByDistrictIdAndBranchId(@Param("districtId") String districtId,
                        @Param("branchId") String branchId);

        // Find by district, serial number, and status (for unique constraint checking)
        @Query("SELECT s FROM SerialNumber s WHERE s.district = :district AND s.serialNumber = :serialNumber AND s.status = :status")
        Optional<SerialNumber> findByDistrictAndSerialNumberAndStatus(
                        @Param("district") District district,
                        @Param("serialNumber") Long serialNumber,
                        @Param("status") SerialNumberStatus status);

        @Query("SELECT s FROM SerialNumber s WHERE s.district = :district AND (:branch IS NULL OR s.branch = :branch) AND s.serialNumber = :serialNumber AND s.status = :status")
        Optional<SerialNumber> findByDistrictAndBranchAndSerialNumberAndStatus(
                        @Param("district") District district,
                        @Param("branch") Branch branch,
                        @Param("serialNumber") Long serialNumber,
                        @Param("status") SerialNumberStatus status);

        // Find all voided entries for a specific serial+district
        @Query("SELECT s FROM SerialNumber s WHERE s.district = :district AND (:branch IS NULL OR s.branch = :branch) AND s.serialNumber = :serialNumber AND s.status = 'VOIDED'")
        List<SerialNumber> findAllVoidedByDistrictAndSerialNumber(
                        @Param("district") District district,
                        @Param("branch") Branch branch,
                        @Param("serialNumber") Long serialNumber);

}