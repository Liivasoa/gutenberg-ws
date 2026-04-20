package mg.msys.gutenber_ws.webservice.category.application;

import mg.msys.gutenber_ws.webservice.facet.dto.FacetValueBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryQueryUseCase {
    Page<FacetValueBookCountDto> list(Pageable pageable);
}
