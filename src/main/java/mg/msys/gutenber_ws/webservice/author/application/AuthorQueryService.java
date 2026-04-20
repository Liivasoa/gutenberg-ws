package mg.msys.gutenber_ws.webservice.author.application;

import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import mg.msys.gutenber_ws.webservice.author.repository.AuthorBookCountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuthorQueryService implements AuthorQueryUseCase {

    private final AuthorBookCountRepository repository;

    public AuthorQueryService(AuthorBookCountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<AuthorBookCountDto> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
