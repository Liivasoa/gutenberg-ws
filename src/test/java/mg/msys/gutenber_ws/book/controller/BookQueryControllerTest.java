package mg.msys.gutenber_ws.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import mg.msys.gutenber_ws.book.application.BookQueryUseCase;
import mg.msys.gutenber_ws.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookQueryController - HTTP contract")
class BookQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookQueryUseCase service;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BookQueryController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private BookDetailDto sampleBook() {
        return new BookDetailDto(11L, "Alice's Adventures in Wonderland",
                List.of("Lewis CARROLL"), List.of("en"),
                List.of("Children's literature"), List.of("Fantasy", "Adventure"));
    }

    @Test
    @DisplayName("shouldReturn200FilteredByLanguage")
    void shouldReturn200FilteredByLanguage() throws Exception {
        when(service.findBooks(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20, Sort.by("title")), 1L));

        mockMvc.perform(get("/api/v1/book").param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11))
                .andExpect(jsonPath("$.content[0].title").value("Alice's Adventures in Wonderland"))
                .andExpect(jsonPath("$.content[0].authors[0]").value("Lewis CARROLL"))
                .andExpect(jsonPath("$.content[0].languages[0]").value("en"))
                .andExpect(jsonPath("$.content[0].categories[0]").value("Children's literature"))
                .andExpect(jsonPath("$.content[0].topics[0]").value("Fantasy"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("shouldReturn200FilteredByTopicId")
    void shouldReturn200FilteredByTopicId() throws Exception {
        when(service.findBooks(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20, Sort.by("title")), 1L));

        mockMvc.perform(get("/api/v1/book").param("topicId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11));
    }

    @Test
    @DisplayName("shouldReturn200FilteredByCategoryId")
    void shouldReturn200FilteredByCategoryId() throws Exception {
        when(service.findBooks(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20, Sort.by("title")), 1L));

        mockMvc.perform(get("/api/v1/book").param("categoryId", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11));
    }

    @Test
    @DisplayName("shouldReturn200FilteredByAuthorId")
    void shouldReturn200FilteredByAuthorId() throws Exception {
        when(service.findBooks(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20, Sort.by("title")), 1L));

        mockMvc.perform(get("/api/v1/book").param("authorId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11));
    }

    @Test
    @DisplayName("shouldReturn200FilteredByBookId")
    void shouldReturn200FilteredByBookId() throws Exception {
        when(service.findBooks(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20, Sort.by("title")), 1L));

        mockMvc.perform(get("/api/v1/book").param("bookId", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11));
    }

    @Test
    @DisplayName("shouldReturn200WithCombinedFilters")
    void shouldReturn200WithCombinedFilters() throws Exception {
        when(service.findBooks(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleBook()), PageRequest.of(0, 20, Sort.by("title")), 1L));

        mockMvc.perform(get("/api/v1/book")
                .param("language", "en")
                .param("topicId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11));
    }

    @Test
    @DisplayName("shouldReturn400WhenNoFilterProvided")
    void shouldReturn400WhenNoFilterProvided() throws Exception {
        mockMvc.perform(get("/api/v1/book"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("shouldReturn400WhenSortByInvalid")
    void shouldReturn400WhenSortByInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/book")
                .param("language", "en")
                .param("sortBy", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("shouldReturn400WhenPageSizeExceedsMax")
    void shouldReturn400WhenPageSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/api/v1/book")
                .param("language", "en")
                .param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
