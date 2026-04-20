package mg.msys.gutenber_ws.language.controller;

import java.util.Set;

import mg.msys.gutenber_ws.language.application.LanguageQueryUseCase;
import mg.msys.gutenber_ws.language.dto.LanguageBookCountDto;
import mg.msys.gutenber_ws.shared.dto.ApiError;
import mg.msys.gutenber_ws.shared.dto.ApiPage;
import mg.msys.gutenber_ws.shared.dto.PageRequestBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Languages", description = "Book count statistics per language")
@SecurityRequirement(name = "oauth2")
@RestController
@RequestMapping("/api/v1/language")
public class LanguageQueryController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "bookCount");

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

        Page<LanguageBookCountDto> result = service.list(
                PageRequestBuilder.build(page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(ApiPage.from(result));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint is working!");
    }
}
