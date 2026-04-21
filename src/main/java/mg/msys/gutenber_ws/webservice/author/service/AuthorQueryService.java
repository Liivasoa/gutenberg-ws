package mg.msys.gutenber_ws.webservice.author.service;

import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorQueryService {
    Page<AuthorBookCountDto> list(Pageable pageable);
}
