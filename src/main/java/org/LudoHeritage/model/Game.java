package org.LudoHeritage.model;

public record Game(
        Long id,
        String name,
        String region,
        String country,
        String period,
        String type,
        String difficulty,
        String description,
        String rules,
        String history,
        String imageUrl,
        String players,
        String duration,
        String age,
        String year,
        String author,
        String reconstructionStatus,
        boolean playable,
        String ludemeSummary,
        java.util.List<String> aliases,
        java.util.List<String> categories,
        java.util.List<String> ludemes,
        java.util.List<String> references,
        java.util.List<GameTutorialStep> tutorialSteps
) {
    public Game(
            Long id,
            String name,
            String region,
            String country,
            String type,
            String difficulty,
            String description,
            String rules,
            String history,
            String imageUrl
    ) {
        this(
                id,
                name,
                region,
                country,
                "Modern",
                type,
                difficulty,
                description,
                rules,
                history,
                imageUrl,
                "2 joueurs",
                "20-40 min",
                "8+",
                "Traditionnel",
                "Tradition orale",
                "Done",
                true,
                type,
                java.util.List.of(),
                java.util.List.of("Board", type),
                java.util.List.of(type),
                java.util.List.of(),
                java.util.List.of()
        );
    }
}
