package com.bankofabyssinia.letter_serial_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bankofabyssinia.letter_serial_backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.district LEFT JOIN FETCH u.branch WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.district LEFT JOIN FETCH u.branch")
    @Override
    List<User> findAll();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.district LEFT JOIN FETCH u.branch WHERE u.id = :id")
    @Override
    Optional<User> findById(@Param("id") String id);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.district LEFT JOIN FETCH u.branch WHERE u.district.id = :districtId")
    List<User> findByDistrictId(@Param("districtId") String districtId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.district LEFT JOIN FETCH u.branch WHERE u.branch.id = :branchId")
    List<User> findByBranchId(@Param("branchId") String branchId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") String id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.district LEFT JOIN FETCH u.branch " +
            "WHERE u.district.id = :districtId AND u.isActive = true")
    List<User> findActiveByDistrictId(@Param("districtId") String districtId);

    @Modifying
    @Query("UPDATE User u SET u.passwordResetSessionId = NULL, u.passwordResetExpiresAt = NULL " +
            "WHERE u.passwordResetExpiresAt IS NOT NULL AND u.passwordResetExpiresAt < :now")
    int deleteExpiredPasswordResetSessions(@Param("now") LocalDateTime now);

    @Query("SELECT u.branch.id FROM User u WHERE u.email = :email")
    Optional<String> findBranchIdByEmail(@Param("email") String email);
}