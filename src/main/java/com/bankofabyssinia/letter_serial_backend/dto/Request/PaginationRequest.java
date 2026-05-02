package com.bankofabyssinia.letter_serial_backend.dto.Request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PaginationRequest implements BaseRequest {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(200)
    private int size = 20;

    private String sortBy = "id";
    private String sortDir = "desc";

    public Pageable toPageable() {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
