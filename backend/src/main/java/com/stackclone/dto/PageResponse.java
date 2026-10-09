package com.stackclone.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Pagination stable pour le frontend (évite de sérialiser PageImpl directement). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
