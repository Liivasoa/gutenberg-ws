package mg.msys.gutenber_ws.book.controller;

import java.util.Set;

import mg.msys.gutenber_ws.book.application.BookQueryUseCase;
import mg.msys.gutenber_ws.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.book.query.BookFilter;
import mg.msys.gutenber_ws.shared.dto.ApiError;
import mg.msys.gutenber_ws.shared.dto.ApiPage;
import mg.msys.gutenber_ws.shared.dto.PageRequestBuilder;
import mg.msys.gutenber_ws.shared.exception.BadRequestException;

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

@Tag(name = "Books", description = "Book detail queries with filters")
@SecurityRequirement(name = "oauth2")
@RestController
@RequestMapping("/api/v1/book")
public class BookQueryController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("title", "id");
    private static final int MAX_PAGE_SIZE = 50;

    private final BookQueryUseCase service;

    public BookQueryController(BookQueryUseCase service) {
        this.service = service;
    }

    @Operation(summary = "List books with details", description = "Returns a paginated list of books with authors, languages, categories and topics. At least one filter is required.", responses = {
            @ApiResponse(responseCode = "200", description = "Paginated list of books"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing filter parameter", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<ApiPage<BookDetailDto>> list(
            @Parameter(description = "Filter by language code, e.g. 'en'") @RequestParam(required = false) String language,
            @Parameter(description = "Filter by topic facet_value id") @RequestParam(required = false) Long topicId,
            @Parameter(description = "Filter by category facet_value id") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by author id") @RequestParam(required = false) Long authorId,
            @Parameter(description = "Filter by book id") @RequestParam(required = false) Long bookId,
            @Parameter(description = "Page number, 0-based", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, max 50", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field: title or id", example = "title") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {

        BookFilter filter = BookFilter.builder()
                .language(language)
                .topicId(topicId)
                .categoryId(categoryId)
                .authorId(authorId)
                .bookId(bookId)
                .build();

        if (filter.isEmpty()) {
            throw new BadRequestException(
                    "At least one filter is required: language, topicId, categoryId, authorId, bookId");
        }

        Page<BookDetailDto> result = service.findBooks(filter,
                PageRequestBuilder.build(page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS, MAX_PAGE_SIZE));
        return ResponseEntity.ok(ApiPage.from(result));
    }
}
