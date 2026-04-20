package mg.msys.gutenber_ws.webservice.language.application;

import mg.msys.gutenber_ws.webservice.language.dto.LanguageBookCountDto;
import mg.msys.gutenber_ws.webservice.language.repository.LanguageBookCountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LanguageQueryService implements LanguageQueryUseCase {

    private final LanguageBookCountRepository repository;

    public LanguageQueryService(LanguageBookCountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<LanguageBookCountDto> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
