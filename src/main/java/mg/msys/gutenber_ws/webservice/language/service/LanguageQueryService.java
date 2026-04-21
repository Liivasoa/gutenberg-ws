package mg.msys.gutenber_ws.webservice.language.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import mg.msys.gutenber_ws.webservice.language.dto.LanguageBookCountDto;

public interface LanguageQueryService {
    public Page<LanguageBookCountDto> list(Pageable pageable);
}
