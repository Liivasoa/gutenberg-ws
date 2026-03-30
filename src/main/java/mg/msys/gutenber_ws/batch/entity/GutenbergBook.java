package mg.msys.gutenber_ws.batch.entity;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GutenbergBook {
    private Long id;
    private LocalDate issued;
    private String title;
    private String languages;
    private String authors;
    private String subjects;
    private String bookshelves;
}
