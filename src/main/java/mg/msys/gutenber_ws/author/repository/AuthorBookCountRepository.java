package mg.msys.gutenber_ws.author.repository;

import mg.msys.gutenber_ws.author.dto.AuthorBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorBookCountRepository {
    Page<AuthorBookCountDto> findAll(Pageable pageable);
}
