package com.bankofabyssinia.letter_serial_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.bankofabyssinia.letter_serial_backend.entity.BaseEntity;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<?>, ID> extends JpaRepository<T, ID> {
}
