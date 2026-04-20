package mg.msys.gutenber_ws.webservice.author.application;

import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorQueryUseCase {
    Page<AuthorBookCountDto> list(Pageable pageable);
}
