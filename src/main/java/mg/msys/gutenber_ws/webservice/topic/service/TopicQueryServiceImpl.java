package mg.msys.gutenber_ws.webservice.topic.service;

import mg.msys.gutenber_ws.webservice.facet.dto.FacetValueBookCountDto;
import mg.msys.gutenber_ws.webservice.facet.repository.FacetValueBookCountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TopicQueryServiceImpl implements TopicQueryService {

    private final FacetValueBookCountRepository repository;

    public TopicQueryServiceImpl(FacetValueBookCountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<FacetValueBookCountDto> list(Pageable pageable) {
        return repository.findAllByFacetCode("topic", pageable);
    }
}
