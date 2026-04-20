package mg.msys.gutenber_ws.webservice.book.dto;

import java.util.List;

public record BookDetailDto(
        long id,
        String title,
        List<String> authors,
        List<String> languages,
        List<String> categories,
        List<String> topics) {
}
