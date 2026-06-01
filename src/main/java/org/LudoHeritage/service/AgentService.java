package org.LudoHeritage.service;

import org.LudoHeritage.model.Game;
import org.LudoHeritage.agent.UserProfile;
import org.LudoHeritage.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final OllamaChatClient llm;
    private final GameService gameService;
    private static final int MAX_GAMES = 5;

    public AgentService(OllamaChatClient llm, GameService gameService) {
        this.llm = llm;
        this.gameService = gameService;
    }

    public ChatResponse chat(String userId, String message, List<?> history, UserProfile profile) {
        var reasoning = new ArrayList<String>();
        String normalized = norm(message);

        if (normalized.isBlank()) {
            return reply("Bonjour ! Je suis LudoBot. Pose-moi une question sur les jeux de table !", reasoning);
        }

        if (isGreeting(normalized)) {
            return reply("Bonjour ! Je suis LudoBot. Je peux recommander un jeu, expliquer des regles, comparer deux jeux ou faire un quiz.", reasoning);
        }

        if (isWeatherQuestion(normalized)) {
            return reply("Je ne peux pas donner la meteo en direct. Je suis specialise dans les jeux de table traditionnels.", reasoning);
        }

        // ── Detect intent ────────────────────────────────────────────────
        Intent intent = detectIntent(normalized);
        reasoning.add("Intent: " + intent);

        // ── Detect sentiment ─────────────────────────────────────────────
        Sentiment sentiment = detectSentiment(normalized);
        reasoning.add("Sentiment: " + sentiment);

        // ── Build system + user prompts ──────────────────────────────────
        String systemPrompt = buildSystemPrompt(intent, sentiment);
        String userPrompt;
        List<Game> relevantGames = List.of();

        if (intent == Intent.GENERAL) {
            userPrompt = message;
        } else if (intent == Intent.GAME_OF_DAY) {
            relevantGames = getGameOfTheDay(profile);
            userPrompt = buildGamePrompt("Quel jeu me recommandes-tu aujourd'hui ?", profile, relevantGames);
        } else if (intent == Intent.COMPARE) {
            relevantGames = findGamesForComparison(normalized);
            userPrompt = buildComparePrompt(message, relevantGames);
        } else if (intent == Intent.QUIZ) {
            relevantGames = findGames(normalized, profile);
            userPrompt = buildQuizPrompt(message, relevantGames);
        } else if (intent == Intent.STRATEGY) {
            relevantGames = findGames(normalized, profile);
            userPrompt = buildStrategyPrompt(message, relevantGames);
        } else if (intent == Intent.CULTURE) {
            relevantGames = findGames(normalized, profile);
            userPrompt = buildCulturePrompt(message, relevantGames);
        } else {
            relevantGames = findGames(normalized, profile);
            userPrompt = buildGamePrompt(message, profile, relevantGames);
        }

        // ── Call LLM ─────────────────────────────────────────────────────
        try {
            String reply = llm.chat(systemPrompt, userPrompt);
            reasoning.add("LLM responded OK");
            return new ChatResponse(cleanReply(reply), reasoning, relevantGames);
        } catch (Exception e) {
            reasoning.add("LLM error: " + e.getMessage());
            return new ChatResponse(fallback(intent, relevantGames, sentiment), reasoning, relevantGames);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  INTENT DETECTION (expanded with 4 new intents)
    // ════════════════════════════════════════════════════════════════════

    private Intent detectIntent(String n) {
        // Compare intent
        if (has(n, "compare", "comparer", "versus", " vs ", "difference", "différence", "plutot", "ou bien"))
            return Intent.COMPARE;
        // Quiz intent
        if (has(n, "quiz", "teste", "tester", "question", "devinette", "qcm", "test"))
            return Intent.QUIZ;
        // Strategy intent
        if (has(n, "strategie", "stratégie", "strategy", "astuce", "conseil", "tip", "tactique", "gagner", "win"))
            return Intent.STRATEGY;
        // Culture intent
        if (has(n, "culture", "culturel", "signification", "symbolique", "rituel", "tradition", "spirituel", "sacre"))
            return Intent.CULTURE;
        // Game of the day
        if (has(n, "jeu du jour", "game of the day", "daily game", "suggestion du jour", "surprise", "decouvrir un jeu"))
            return Intent.GAME_OF_DAY;
        // Standard intents
        if (has(n, "recommand", "suggest", "conseill", "propose", "quel jeu", "which game"))
            return Intent.RECOMMENDATION;
        if (has(n, "regle", "règle", "rule", "comment jouer", "how to play", "apprendre"))
            return Intent.RULES;
        if (has(n, "histoire", "origine", "history", "origin", "culture", "invention"))
            return Intent.HISTORY;
        if (mentionsGame(n))
            return Intent.GAME_CHAT;
        return Intent.GENERAL;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SENTIMENT DETECTION
    // ════════════════════════════════════════════════════════════════════

    private Sentiment detectSentiment(String n) {
        if (has(n, "merci", "genial", "super", "excellent", "parfait", "cool", "wow", "bravo", "adore", "love", "great", "amazing", "thanks"))
            return Sentiment.POSITIVE;
        if (has(n, "nul", "mauvais", "decevant", "ennuyeux", "boring", "bad", "horrible", "deteste", "hate", "marre", "frustre"))
            return Sentiment.NEGATIVE;
        if (has(n, "aide", "help", "comprends pas", "perdu", "confused", "difficile", "dur"))
            return Sentiment.CONFUSED;
        return Sentiment.NEUTRAL;
    }

    private boolean mentionsGame(String n) {
        for (Game g : gameService.getAllGames(null, null, null, null)) {
            if (n.contains(g.name().toLowerCase(Locale.ROOT))) return true;
        }
        return has(n, "jeu", "jeux", "game", "jouer", "play", "plateau", "board",
                "strategie", "strategy", "carte", "cartes", "pion", "pions",
                "mancala", "awele", "echecs", "chess", "backgammon", "domino",
                "go", "shogi", "dames", "senet", "tablut");
    }

    // ════════════════════════════════════════════════════════════════════
    //  PROMPT BUILDING (enhanced with personality + sentiment)
    // ════════════════════════════════════════════════════════════════════

    private String buildSystemPrompt(Intent intent, Sentiment sentiment) {
        String mood = switch (sentiment) {
            case POSITIVE -> " L'utilisateur est enthousiaste — partage son energie !";
            case NEGATIVE -> " L'utilisateur semble frustre — sois tres empathique, patient et encourageant.";
            case CONFUSED -> " L'utilisateur semble perdu — sois extra clair, utilise des exemples simples.";
            case NEUTRAL -> "";
        };

        return switch (intent) {
            case GENERAL -> """
                    Tu es LudoBot, un assistant passionné spécialisé dans les jeux de table traditionnels du monde.
                    Tu as une personnalité chaleureuse et cultivée. Tu adores partager des anecdotes.
                    Tu peux répondre aux salutations et questions générales naturellement.
                    Si hors sujet (programmation, médecine...), décline poliment et propose de parler de jeux.
                    Réponds en 2-4 phrases. Utilise la même langue que l'utilisateur.""" + mood;
            case RECOMMENDATION -> """
                    Tu es LudoBot, expert passionné en jeux de table traditionnels.
                    Recommande un jeu adapté au profil. Compare 2 options max puis donne ta recommandation.
                    Sois enthousiaste et explique POURQUOI ce jeu leur conviendrait. 3-5 phrases.""" + mood;
            case RULES -> """
                    Tu es LudoBot, expert en jeux de table traditionnels.
                    Explique les règles de façon claire, par étapes numérotées.
                    Utilise des analogies pour les concepts complexes. 4-6 phrases.""" + mood;
            case HISTORY -> """
                    Tu es LudoBot, conteur passionné de l'histoire des jeux.
                    Raconte l'histoire de façon captivante avec des anecdotes fascinantes.
                    Connecte le passé au présent. 3-5 phrases.""" + mood;
            case COMPARE -> """
                    Tu es LudoBot, expert comparatif en jeux de table.
                    Compare les jeux demandés sur ces axes : complexité, durée, stratégie, accessibilité, histoire.
                    Donne un verdict final personnalisé. Utilise des émojis pour rendre ça vivant. 5-8 phrases.""" + mood;
            case QUIZ -> """
                    Tu es LudoBot, animateur de quiz ludique sur les jeux de table.
                    Pose UNE question de quiz intéressante sur le jeu mentionné (ou un jeu au hasard).
                    Donne 4 options (A, B, C, D) et la bonne réponse à la fin avec une explication.
                    Rends ça fun et éducatif !""" + mood;
            case STRATEGY -> """
                    Tu es LudoBot, coach stratégique en jeux de table traditionnels.
                    Donne 3-4 conseils stratégiques concrets et actionnables pour le jeu demandé.
                    Inclus un conseil pour débutant ET un conseil avancé. Utilise des numéros.""" + mood;
            case CULTURE -> """
                    Tu es LudoBot, anthropologue culturel spécialisé dans les jeux de table.
                    Explique la signification culturelle profonde du jeu : rituels, symboles, rôle social.
                    Connecte aux valeurs de la communauté d'origine. 4-6 phrases captivantes.""" + mood;
            case GAME_OF_DAY -> """
                    Tu es LudoBot. Présente le "Jeu du Jour" avec enthousiasme !
                    Explique pourquoi ce jeu est spécial et pourquoi l'utilisateur devrait l'essayer aujourd'hui.
                    Ajoute une anecdote fascinante. 3-5 phrases.""" + mood;
            case GAME_CHAT -> """
                    Tu es LudoBot, expert en jeux de table traditionnels.
                    Réponds à la question sur le jeu en utilisant le contexte fourni.
                    Sois précis et passionné. 2-4 phrases.""" + mood;
        };
    }

    // ── Specialized prompt builders ──────────────────────────────────────

    private String buildGamePrompt(String message, UserProfile profile, List<Game> games) {
        var sb = new StringBuilder();
        sb.append("Question: ").append(message).append("\n\n");
        appendProfile(sb, profile);
        appendGames(sb, games);
        return sb.toString();
    }

    private String buildComparePrompt(String message, List<Game> games) {
        var sb = new StringBuilder();
        sb.append("Demande de comparaison: ").append(message).append("\n\n");
        if (games.size() >= 2) {
            sb.append("Jeu A: ").append(games.get(0).name()).append(" — ").append(games.get(0).description()).append("\n");
            sb.append("  Region: ").append(games.get(0).region()).append(", Type: ").append(games.get(0).type())
              .append(", Difficulte: ").append(games.get(0).difficulty()).append("\n\n");
            sb.append("Jeu B: ").append(games.get(1).name()).append(" — ").append(games.get(1).description()).append("\n");
            sb.append("  Region: ").append(games.get(1).region()).append(", Type: ").append(games.get(1).type())
              .append(", Difficulte: ").append(games.get(1).difficulty()).append("\n");
        } else {
            appendGames(sb, games);
        }
        return sb.toString();
    }

    private String buildQuizPrompt(String message, List<Game> games) {
        var sb = new StringBuilder();
        sb.append("Cree un quiz sur: ").append(message).append("\n\n");
        appendGames(sb, games);
        return sb.toString();
    }

    private String buildStrategyPrompt(String message, List<Game> games) {
        var sb = new StringBuilder();
        sb.append("Demande de strategie: ").append(message).append("\n\n");
        appendGames(sb, games);
        return sb.toString();
    }

    private String buildCulturePrompt(String message, List<Game> games) {
        var sb = new StringBuilder();
        sb.append("Demande culturelle: ").append(message).append("\n\n");
        appendGames(sb, games);
        return sb.toString();
    }

    private void appendProfile(StringBuilder sb, UserProfile profile) {
        if (profile != null) {
            sb.append("Profil joueur:\n");
            if (profile.getNiveau() != null) sb.append("- Niveau: ").append(profile.getNiveau()).append("\n");
            if (profile.getTypeJeuPrefere() != null) sb.append("- Type prefere: ").append(profile.getTypeJeuPrefere()).append("\n");
            if (profile.getRegionPreferee() != null) sb.append("- Region preferee: ").append(profile.getRegionPreferee()).append("\n");
            if (profile.getLangue() != null) sb.append("- Langue: ").append(profile.getLangue()).append("\n");
            sb.append("\n");
        }
    }

    private void appendGames(StringBuilder sb, List<Game> games) {
        if (!games.isEmpty()) {
            sb.append("Jeux disponibles:\n");
            for (Game g : games) {
                sb.append("- ").append(g.name())
                        .append(" (").append(g.region()).append(", ").append(g.type()).append(", ").append(g.difficulty()).append(")")
                        .append("\n  ").append(g.description()).append("\n");
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    //  GAME SEARCH & SPECIAL FINDERS
    // ════════════════════════════════════════════════════════════════════

    private List<Game> findGames(String normalized, UserProfile profile) {
        List<Game> found = new ArrayList<>(gameService.searchGames(normalized, null, null));
        if (profile != null) {
            var extra = gameService.getAllGames(null, profile.getRegionPreferee(),
                    profile.getTypeJeuPrefere(), profile.getNiveau());
            for (Game g : extra) {
                if (found.stream().noneMatch(x -> x.id().equals(g.id()))) found.add(g);
            }
        }
        if (found.isEmpty()) {
            found.addAll(gameService.getAllGames(null, null, null, null));
        }
        return found.stream().sorted(Comparator.comparing(Game::name)).limit(MAX_GAMES).collect(Collectors.toList());
    }

    private List<Game> findGamesForComparison(String normalized) {
        List<Game> all = gameService.getAllGames(null, null, null, null);
        List<Game> matched = new ArrayList<>();
        for (Game g : all) {
            if (normalized.contains(g.name().toLowerCase(Locale.ROOT))) {
                matched.add(g);
            }
        }
        if (matched.size() < 2) {
            // Try to find two games from the full list
            matched = all.stream().limit(2).collect(Collectors.toList());
        }
        return matched.stream().limit(2).collect(Collectors.toList());
    }

    private List<Game> getGameOfTheDay(UserProfile profile) {
        List<Game> all = gameService.getAllGames(null, null, null, null);
        if (all.isEmpty()) return List.of();
        // Use day-of-year as index to rotate through games
        int dayIndex = LocalDate.now().getDayOfYear() % all.size();
        return List.of(all.get(dayIndex));
    }

    // ════════════════════════════════════════════════════════════════════
    //  FALLBACK RESPONSES (sentiment-aware)
    // ════════════════════════════════════════════════════════════════════

    private String fallback(Intent intent, List<Game> games, Sentiment sentiment) {
        var names = games.stream().map(Game::name).collect(Collectors.joining(", "));

        String prefix = switch (sentiment) {
            case NEGATIVE -> "Je comprends ta frustration. ";
            case CONFUSED -> "Pas de souci, je suis la pour t'aider ! ";
            case POSITIVE -> "Super ! ";
            case NEUTRAL -> "";
        };

        return prefix + switch (intent) {
            case GENERAL -> "Je suis LudoBot, ton guide pour les jeux de table traditionnels. Que veux-tu decouvrir ?";
            case RECOMMENDATION -> names.isEmpty()
                    ? "Dis-moi tes preferences et je te recommanderai un jeu !"
                    : "Je te recommande de decouvrir " + names + ". Dis-moi tes preferences pour affiner !";
            case RULES -> names.isEmpty()
                    ? "De quel jeu veux-tu connaitre les regles ?"
                    : "Je connais les regles de " + names + ". Lequel t'interesse ?";
            case HISTORY -> names.isEmpty()
                    ? "De quel jeu veux-tu connaitre l'histoire ?"
                    : "Je peux te raconter l'histoire de " + names + ". Lequel ?";
            case COMPARE -> "Dis-moi quels jeux tu veux comparer ! Par exemple : 'Compare Awele vs Go'";
            case QUIZ -> names.isEmpty()
                    ? "Sur quel jeu veux-tu etre teste ?"
                    : "Je peux te poser des questions sur " + names + " ! Pret ?";
            case STRATEGY -> names.isEmpty()
                    ? "De quel jeu veux-tu des conseils strategiques ?"
                    : "Voici mes conseils pour " + names + " : commence par maitriser les bases !";
            case CULTURE -> names.isEmpty()
                    ? "De quel jeu veux-tu connaitre la signification culturelle ?"
                    : "La culture de " + names + " est fascinante ! Pose-moi une question precise.";
            case GAME_OF_DAY -> names.isEmpty()
                    ? "Reviens demain pour une nouvelle suggestion !"
                    : "Le jeu du jour est " + names + " ! Un classique a decouvrir.";
            case GAME_CHAT -> names.isEmpty()
                    ? "Pose-moi une question sur un jeu de table traditionnel !"
                    : "Je peux t'aider avec " + names + ". Que veux-tu savoir ?";
        };
    }

    // ════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ════════════════════════════════════════════════════════════════════

    private ChatResponse reply(String text, List<String> reasoning) {
        return new ChatResponse(text, reasoning, List.of());
    }

    private String cleanReply(String reply) {
        return reply.replace("\r", "").trim();
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private boolean has(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private boolean isGreeting(String n) {
        return n.matches("^(bonjour|bojour|bonsoir|salut|salam|hello|hi|hey|coucou)[ !?.]*$");
    }

    private boolean isWeatherQuestion(String n) {
        return has(n, "weather", "temperature", "temp today", "meteo", "mÃ©tÃ©o");
    }

    private enum Intent {
        GENERAL, RECOMMENDATION, RULES, HISTORY, GAME_CHAT,
        COMPARE, QUIZ, STRATEGY, CULTURE, GAME_OF_DAY
    }

    private enum Sentiment {
        POSITIVE, NEGATIVE, CONFUSED, NEUTRAL
    }
}
