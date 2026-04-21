package mg.msys.gutenber_ws.webservice.book.repository;

import mg.msys.gutenber_ws.webservice.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.webservice.book.query.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookQueryRepository {
    Page<BookDetailDto> findBooks(BookFilter filter, Pageable pageable);
}
