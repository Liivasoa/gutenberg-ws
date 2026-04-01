package mg.msys.gutenber_ws.shared.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record ApiPage<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> ApiPage<T> from(Page<T> page) {
        return new ApiPage<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public static <S, T> ApiPage<T> from(Page<S> page, Function<S, T> mapper) {
        return new ApiPage<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
