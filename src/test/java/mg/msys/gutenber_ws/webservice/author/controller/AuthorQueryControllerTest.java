package mg.msys.gutenber_ws.webservice.author.controller;

import mg.msys.gutenber_ws.webservice.author.application.AuthorQueryUseCase;
import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import mg.msys.gutenber_ws.webservice.shared.exception.GlobalExceptionHandler;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorQueryController - HTTP contract")
class AuthorQueryControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AuthorQueryUseCase service;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(new AuthorQueryController(service))
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        @DisplayName("shouldReturn200WithPaginatedAuthorBookCounts")
        void shouldReturn200WithPaginatedAuthorBookCounts() throws Exception {
                when(service.list(any())).thenReturn(
                                new PageImpl<>(List.of(new AuthorBookCountDto(1L, "Wilde", "Oscar", 42L)),
                                                PageRequest.of(0, 20, Sort.by("lastName")), 1L));

                mockMvc.perform(get("/api/v1/author")
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "lastName")
                                .param("sortDir", "asc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].lastName").value("Wilde"))
                                .andExpect(jsonPath("$.content[0].firstNames").value("Oscar"))
                                .andExpect(jsonPath("$.content[0].bookCount").value(42))
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(20))
                                .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("shouldUseDefaultPaginationWhenNoParamsGiven")
        void shouldUseDefaultPaginationWhenNoParamsGiven() throws Exception {
                when(service.list(any())).thenReturn(
                                new PageImpl<>(List.of(new AuthorBookCountDto(2L, "Austen", "Jane", 12L)),
                                                PageRequest.of(0, 20, Sort.by("lastName")), 1L));

                mockMvc.perform(get("/api/v1/author"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.page").value(0))
                                .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("shouldRejectUnsupportedSortField")
        void shouldRejectUnsupportedSortField() throws Exception {
                mockMvc.perform(get("/api/v1/author")
                                .param("sortBy", "invalid"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").isNotEmpty());
        }

        @Test
        @DisplayName("shouldRejectOversizedPageSize")
        void shouldRejectOversizedPageSize() throws Exception {
                mockMvc.perform(get("/api/v1/author")
                                .param("size", "101"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }
}
