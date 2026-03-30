package mg.msys.gutenber_ws.batch.entity;

public record Author(String lastName, String firstNames, Integer birthYear, Integer deathYear, String normalizedKey) {
}
