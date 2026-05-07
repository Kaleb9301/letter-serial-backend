package com.bankofabyssinia.letter_serial_backend.entity;

import java.time.LocalDateTime;

import com.bankofabyssinia.letter_serial_backend.annotation.FlexibleId;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class BaseEntity<ID> {

    @Id
    @FlexibleId
    @Column(name = "id", updatable = false, nullable = false)
    protected ID id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
