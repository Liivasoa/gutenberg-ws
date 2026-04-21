package mg.msys.gutenber_ws.webservice.author.service;

import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import mg.msys.gutenber_ws.webservice.author.repository.AuthorBookCountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuthorQueryServiceImpl implements AuthorQueryService {

    private final AuthorBookCountRepository repository;

    public AuthorQueryServiceImpl(AuthorBookCountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<AuthorBookCountDto> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
