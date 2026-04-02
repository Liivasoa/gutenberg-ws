package mg.msys.gutenber_ws.topic.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import mg.msys.gutenber_ws.facet.dto.FacetValueBookCountDto;
import mg.msys.gutenber_ws.shared.exception.GlobalExceptionHandler;
import mg.msys.gutenber_ws.topic.application.TopicQueryUseCase;
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
@DisplayName("TopicQueryController - HTTP contract")
class TopicQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TopicQueryUseCase service;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TopicQueryController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("shouldReturn200WithPaginatedTopicBookCounts")
    void shouldReturn200WithPaginatedTopicBookCounts() throws Exception {
        when(service.list(any())).thenReturn(
                new PageImpl<>(List.of(new FacetValueBookCountDto("Fiction", 42L)),
                        PageRequest.of(0, 20, Sort.by("value")), 1L));

        mockMvc.perform(get("/api/v1/topic")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "value")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].value").value("Fiction"))
                .andExpect(jsonPath("$.content[0].bookCount").value(42))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("shouldUseDefaultPaginationWhenNoParamsGiven")
    void shouldUseDefaultPaginationWhenNoParamsGiven() throws Exception {
        when(service.list(any())).thenReturn(
                new PageImpl<>(List.of(new FacetValueBookCountDto("History", 10L)),
                        PageRequest.of(0, 20, Sort.by("value")), 1L));

        mockMvc.perform(get("/api/v1/topic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @DisplayName("shouldRejectUnsupportedSortField")
    void shouldRejectUnsupportedSortField() throws Exception {
        mockMvc.perform(get("/api/v1/topic")
                .param("sortBy", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("shouldRejectOversizedPageSize")
    void shouldRejectOversizedPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/topic")
                .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
