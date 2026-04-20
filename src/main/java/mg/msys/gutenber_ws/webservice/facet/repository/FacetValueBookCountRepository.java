package mg.msys.gutenber_ws.webservice.facet.repository;

import mg.msys.gutenber_ws.webservice.facet.dto.FacetValueBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FacetValueBookCountRepository {
    Page<FacetValueBookCountDto> findAllByFacetCode(String facetCode, Pageable pageable);
}
