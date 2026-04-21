package mg.msys.gutenber_ws.webservice.shared.dto;

import java.util.Set;

import mg.msys.gutenber_ws.webservice.shared.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestBuilder {

    static final int MAX_PAGE_SIZE = 100;

    private PageRequestBuilder() {
    }

    public static PageRequest build(int page, int size, String sortBy, String sortDir,
            Set<String> allowedSortFields) {
        return build(page, size, sortBy, sortDir, allowedSortFields, MAX_PAGE_SIZE);
    }

    public static PageRequest build(int page, int size, String sortBy, String sortDir,
            Set<String> allowedSortFields, int maxPageSize) {
        if (!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException("sortBy must be one of: " + allowedSortFields);
        }
        if (size > maxPageSize) {
            throw new BadRequestException("size must not exceed " + maxPageSize);
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
