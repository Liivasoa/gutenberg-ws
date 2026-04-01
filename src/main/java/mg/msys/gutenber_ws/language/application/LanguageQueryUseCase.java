package mg.msys.gutenber_ws.language.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import mg.msys.gutenber_ws.language.dto.LanguageBookCountDto;

public interface LanguageQueryUseCase {
    public Page<LanguageBookCountDto> list(Pageable pageable);
}
