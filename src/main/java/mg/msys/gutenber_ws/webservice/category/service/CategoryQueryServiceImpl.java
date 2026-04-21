package mg.msys.gutenber_ws.webservice.category.service;

import mg.msys.gutenber_ws.webservice.facet.dto.FacetValueBookCountDto;
import mg.msys.gutenber_ws.webservice.facet.repository.FacetValueBookCountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final FacetValueBookCountRepository repository;

    public CategoryQueryServiceImpl(FacetValueBookCountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<FacetValueBookCountDto> list(Pageable pageable) {
        return repository.findAllByFacetCode("category", pageable);
    }
}
