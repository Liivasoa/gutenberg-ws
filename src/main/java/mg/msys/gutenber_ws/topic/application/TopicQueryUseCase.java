package mg.msys.gutenber_ws.topic.application;

import mg.msys.gutenber_ws.facet.dto.FacetValueBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TopicQueryUseCase {
    Page<FacetValueBookCountDto> list(Pageable pageable);
}
