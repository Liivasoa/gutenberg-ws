package mg.msys.gutenber_ws.topic.application;

import mg.msys.gutenber_ws.facet.dto.FacetValueBookCountDto;
import mg.msys.gutenber_ws.facet.repository.FacetValueBookCountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TopicQueryService implements TopicQueryUseCase {

    private final FacetValueBookCountRepository repository;

    public TopicQueryService(FacetValueBookCountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<FacetValueBookCountDto> list(Pageable pageable) {
        return repository.findAllByFacetCode("topic", pageable);
    }
}
