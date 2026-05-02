package com.bankofabyssinia.letter_serial_backend.mapper;

import java.util.List;

public interface BaseMapper<E, Req, Res> {

    E toEntity(Req request);

    Res toResponse(E entity);

    List<Res> toResponseList(List<E> entities);
}
