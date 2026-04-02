package mg.msys.gutenber_ws.shared.dto;

import java.util.Set;

import mg.msys.gutenber_ws.shared.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestBuilder {

    static final int MAX_PAGE_SIZE = 100;

    private PageRequestBuilder() {
    }

    public static PageRequest build(int page, int size, String sortBy, String sortDir,
            Set<String> allowedSortFields) {
        if (!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException("sortBy must be one of: " + allowedSortFields);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must not exceed " + MAX_PAGE_SIZE);
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
