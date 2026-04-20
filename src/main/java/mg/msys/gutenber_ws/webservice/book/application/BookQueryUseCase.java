package mg.msys.gutenber_ws.webservice.book.application;

import mg.msys.gutenber_ws.webservice.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.webservice.book.query.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookQueryUseCase {
    Page<BookDetailDto> findBooks(BookFilter filter, Pageable pageable);
}
