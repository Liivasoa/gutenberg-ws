package mg.msys.gutenber_ws.language.controller;

import java.util.Set;

import mg.msys.gutenber_ws.language.application.LanguageQueryService;
import mg.msys.gutenber_ws.language.application.LanguageQueryUseCase;
import mg.msys.gutenber_ws.language.dto.LanguageBookCountDto;
import mg.msys.gutenber_ws.shared.dto.ApiError;
import mg.msys.gutenber_ws.shared.dto.ApiPage;
import mg.msys.gutenber_ws.shared.exception.BadRequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Languages", description = "Book count statistics per language")

@RestController
@RequestMapping("/api/v1/language")
public class LanguageQueryController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "bookCount");
    private static final int MAX_PAGE_SIZE = 100;

    private final LanguageQueryUseCase service;

    public LanguageQueryController(LanguageQueryUseCase service) {
        this.service = service;
    }

    @Operation(summary = "List languages with book counts", description = "Returns a paginated list of languages with their associated book count, sortable on code or bookCount.", responses = {
            @ApiResponse(responseCode = "200", description = "Paginated list of languages"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<ApiPage<LanguageBookCountDto>> list(
            @Parameter(description = "Page number, 0-based", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, max 100", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field: code or bookCount", example = "code") @RequestParam(defaultValue = "code") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("sortBy must be one of: " + ALLOWED_SORT_FIELDS);
        }
        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must not exceed " + MAX_PAGE_SIZE);
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Page<LanguageBookCountDto> result = service.list(
                PageRequest.of(page, size, Sort.by(direction, sortBy)));
        return ResponseEntity.ok(ApiPage.from(result));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint is working!");
    }
}
