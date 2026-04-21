package mg.msys.gutenber_ws.webservice.book.service;

import mg.msys.gutenber_ws.webservice.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.webservice.book.query.BookFilter;
import mg.msys.gutenber_ws.webservice.book.repository.BookQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookQueryServiceImpl implements BookQueryService {

    private final BookQueryRepository repository;

    public BookQueryServiceImpl(BookQueryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<BookDetailDto> findBooks(BookFilter filter, Pageable pageable) {
        return repository.findBooks(filter, pageable);
    }
}
