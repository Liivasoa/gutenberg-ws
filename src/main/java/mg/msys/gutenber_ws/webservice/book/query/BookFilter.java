package mg.msys.gutenber_ws.webservice.book.query;

public final class BookFilter {

    private final String language;
    private final Long topicId;
    private final Long categoryId;
    private final Long authorId;
    private final Long bookId;

    private BookFilter(Builder builder) {
        this.language = builder.language;
        this.topicId = builder.topicId;
        this.categoryId = builder.categoryId;
        this.authorId = builder.authorId;
        this.bookId = builder.bookId;
    }

    public String language() {
        return language;
    }

    public Long topicId() {
        return topicId;
    }

    public Long categoryId() {
        return categoryId;
    }

    public Long authorId() {
        return authorId;
    }

    public Long bookId() {
        return bookId;
    }

    public boolean isEmpty() {
        return language == null && topicId == null && categoryId == null
                && authorId == null && bookId == null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String language;
        private Long topicId;
        private Long categoryId;
        private Long authorId;
        private Long bookId;

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder topicId(Long topicId) {
            this.topicId = topicId;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder authorId(Long authorId) {
            this.authorId = authorId;
            return this;
        }

        public Builder bookId(Long bookId) {
            this.bookId = bookId;
            return this;
        }

        public BookFilter build() {
            return new BookFilter(this);
        }
    }
}
