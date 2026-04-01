package mg.msys.gutenber_ws.language.repository;

import mg.msys.gutenber_ws.language.dto.LanguageBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LanguageBookCountRepository {
    Page<LanguageBookCountDto> findAll(Pageable pageable);
}
