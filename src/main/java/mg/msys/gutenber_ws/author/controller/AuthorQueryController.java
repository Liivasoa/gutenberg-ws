package mg.msys.gutenber_ws.author.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mg.msys.gutenber_ws.author.application.AuthorQueryUseCase;
import mg.msys.gutenber_ws.author.dto.AuthorBookCountDto;
import mg.msys.gutenber_ws.shared.dto.ApiPage;
import mg.msys.gutenber_ws.shared.dto.PageRequestBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(name = "Authors", description = "Book count statistics per author")
@SecurityRequirement(name = "oauth2")
@RestController
@RequestMapping("/api/v1/author")
public class AuthorQueryController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("lastName", "firstNames", "bookCount");

    private final AuthorQueryUseCase service;

    public AuthorQueryController(AuthorQueryUseCase service) {
        this.service = service;
    }

    @Operation(summary = "List authors with book counts")
    @GetMapping
    public ResponseEntity<ApiPage<AuthorBookCountDto>> list(
            @Parameter(description = "Page number, 0-based", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, max 100", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field: lastName, firstNames or bookCount", example = "lastName") @RequestParam(defaultValue = "lastName") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {

        Page<AuthorBookCountDto> result = service.list(
                PageRequestBuilder.build(page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS));
        return ResponseEntity.ok(ApiPage.from(result));
    }
}
