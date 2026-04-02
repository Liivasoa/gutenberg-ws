package mg.msys.gutenber_ws.book.application;

import mg.msys.gutenber_ws.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.book.query.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookQueryUseCase {
    Page<BookDetailDto> findBooks(BookFilter filter, Pageable pageable);
}
