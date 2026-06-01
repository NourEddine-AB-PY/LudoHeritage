package org.LudoHeritage.service;

import jakarta.annotation.PostConstruct;
import org.LudoHeritage.model.Game;
import org.LudoHeritage.model.GameDocument;
import org.LudoHeritage.model.GameRepository;
import org.LudoHeritage.model.GameTutorialStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class GameService {
    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final List<Game> fallbackGames = seedGames();

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @PostConstruct
    public void seedDatabase() {
        try {
            // Always reseed so game data (playable flags, descriptions) stays in sync with code.
            gameRepository.deleteAll();
            gameRepository.saveAll(fallbackGames.stream().map(GameDocument::fromGame).toList());
            log.info("Seeded {} LudoHeritage games into MongoDB", fallbackGames.size());
        } catch (Exception ex) {
            log.warn("Mongo game catalog unavailable; using in-memory fallback: {}", ex.getMessage());
        }
    }

    public List<Game> getAllGames(String q, String region, String type, String difficulty) {
        return catalog().stream()
                .filter(game -> q == null || q.isBlank() || matchesQuery(game, q))
                .filter(game -> region == null || region.isBlank() || equalsAny(game.region(), region))
                .filter(game -> type == null || type.isBlank() || equalsAny(game.type(), type) || containsIgnoreCase(game.categories(), type))
                .filter(game -> difficulty == null || difficulty.isBlank() || equalsAny(game.difficulty(), difficulty))
                .sorted(Comparator.comparing(Game::name))
                .toList();
    }

    public Optional<Game> getGameById(Long id) {
        return catalog().stream().filter(game -> game.id().equals(id)).findFirst();
    }

    public List<String> getRegions() {
        return catalog().stream().map(Game::region).distinct().sorted().toList();
    }

    public List<String> getPeriods() {
        return catalog().stream().map(Game::period).distinct().sorted().toList();
    }

    public List<String> getCategories() {
        return catalog().stream().flatMap(game -> game.categories().stream()).distinct().sorted().toList();
    }

    public Game getGameOfTheDay() {
        List<Game> games = catalog().stream().sorted(Comparator.comparing(Game::name)).toList();
        if (games.isEmpty()) throw new IllegalStateException("No games available");
        return games.get(LocalDate.now().getDayOfYear() % games.size());
    }

    public List<Game> searchGames(String q, String region, String type) {
        return getAllGames(q, region, type, null);
    }

    public List<Game> findSimilarGames(Game target) {
        return catalog().stream()
                .filter(game -> !game.id().equals(target.id()))
                .filter(game -> game.type().equalsIgnoreCase(target.type())
                        || game.region().equalsIgnoreCase(target.region())
                        || game.ludemes().stream().anyMatch(target.ludemes()::contains))
                .limit(3)
                .toList();
    }

    private List<Game> catalog() {
        try {
            List<Game> games = gameRepository.findAll().stream().map(GameDocument::toGame).toList();
            return games.isEmpty() ? fallbackGames : games;
        } catch (Exception ex) {
            return fallbackGames;
        }
    }

    private boolean matchesQuery(Game game, String q) {
        String key = q.toLowerCase(Locale.ROOT);
        return contains(game.name(), key)
                || contains(game.region(), key)
                || contains(game.country(), key)
                || contains(game.period(), key)
                || contains(game.type(), key)
                || contains(game.description(), key)
                || contains(game.ludemeSummary(), key)
                || containsIgnoreCase(game.aliases(), key)
                || containsIgnoreCase(game.categories(), key)
                || containsIgnoreCase(game.ludemes(), key);
    }

    private boolean contains(String value, String key) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(key);
    }

    private boolean containsIgnoreCase(List<String> values, String key) {
        return values != null && values.stream().anyMatch(value -> contains(value, key));
    }

    private boolean equalsAny(String value, String expected) {
        return value != null && expected != null && value.equalsIgnoreCase(expected);
    }

    // ── Seed (max 20 games, no haram-history content) ────────────────────
    private List<Game> seedGames() {
        List<Game> games = new ArrayList<>();

        // 1 — Awale
        games.add(game(1L, "Awale", "Afrique", "Afrique de l'Ouest", "Ancient", "Strategie", "Debutant",
                "Jeu de semailles du groupe Mancala joue avec des graines et des trous dans une planche.",
                "Prenez toutes les graines d'une case et distribuez-les une par une dans le sens anti-horaire. Capturez si la derniere graine tombe dans une case adverse contenant 2 ou 3 graines.",
                "L'Awale est transmis oralement de generation en generation en Afrique de l'Ouest. Il est a la fois un jeu social, un exercice de calcul mental et un outil d'education.",
                "2 joueurs", "15-30 min", "6+", "~3000 av. J.-C.", "Tradition orale", "Done", true,
                "Sow, capture, two-row board",
                List.of("Oware", "Awele", "Awari"),
                List.of("Board", "Sow", "Two rows"),
                List.of("sow", "capture", "seeds"),
                List.of("Traditional West African mancala family"),
                tutorial("Choisir une case", "Le joueur choisit une case et ramasse toutes ses graines.",
                        List.of("4 4 4 4 4 4", "4 4 4 4 4 4"), List.of("4 4 0 4 4 4", "4 4 4 4 4 4")),
                tutorial("Semer les graines", "Les graines sont deposees une par une dans les cases suivantes.",
                        List.of("4 4 0 4 4 4", "4 4 4 4 4 4"), List.of("4 4 0 5 5 5", "5 4 4 4 4 4"))));

        // 2 — Mancala (Kalah)
        games.add(game(2L, "Mancala", "Afrique", "Afrique de l'Est", "Ancient", "Strategie", "Debutant",
                "Famille de jeux de semailles joues avec des graines ou des pierres sur un plateau a trous.",
                "Distribuez les graines autour du plateau dans le sens anti-horaire. Terminer dans votre reserve vous donne un tour supplementaire. Celui qui capture le plus gagne.",
                "Le Mancala designe une grande famille de variantes presentes en Afrique, en Asie et aux Ameriques. Le mot vient de l'arabe 'naqala' qui signifie 'deplacer'.",
                "2 joueurs", "10-25 min", "5+", "~700 ap. J.-C.", "Tradition orale", "Done", false,
                "Sow, store, extra turn",
                List.of("Kalah"),
                List.of("Board", "Sow", "Store"),
                List.of("sow", "store", "extra-turn"),
                List.of("Mancala game family"),
                tutorial("La reserve", "Chaque joueur a une reserve de score a sa droite.",
                        List.of("S 4 4 4 4 4 4", "4 4 4 4 4 4 S"), List.of("S 4 4 4 4 4 4", "4 4 4 4 4 4 S")),
                tutorial("Tomber dans la reserve", "Terminer dans votre reserve marque un point et vous rejoue.",
                        List.of("S 0 4 4 4 4 4", "4 4 4 4 4 4 S"), List.of("S+1 0 5 5 5 5 4", "4 4 4 4 4 4 S"))));

        // 3 — Backgammon
        games.add(game(3L, "Backgammon", "Asie", "Iran / Moyen-Orient", "Ancient", "Hasard et strategie", "Debutant",
                "Jeu de course ou les des creent le hasard et les decisions strategiques font la difference.",
                "Lancez les des, deplacez vos pions dans votre sens, bloquez l'adversaire et sortez tous vos pions du plateau en premier.",
                "Des jeux proches du Backgammon sont documentes au Moyen-Orient depuis plusieurs millenaires. Le 'Nard' persan est l'ancetre direct. Le jeu s'est repandu dans tout le monde arabe et mediteraneen.",
                "2 joueurs", "20-40 min", "8+", "~3000 av. J.-C.", "Tradition orale", "Done", false,
                "Race, dice, bear-off, blocking",
                List.of("Nard", "Tables", "Tavli"),
                List.of("Board", "Race", "Dice"),
                List.of("race", "dice", "blocking", "bear-off"),
                List.of("Historical race-game family"),
                tutorial("Lancer les des", "Les des indiquent combien de cases chaque pion peut avancer.",
                        List.of("A . . . . .", ". . . . . B"), List.of("Des: 3 et 2", "")),
                tutorial("Deplacer selon un de", "Un pion avance du nombre de cases indique par un seul de.",
                        List.of("A . . . . .", ". . . . . B"), List.of(". . . A . .", ". . . . . B"))));

        // 4 — Go
        games.add(game(4L, "Go", "Asie", "Chine", "Ancient", "Strategie", "Passionne",
                "Jeu de territoire aux regles simples mais a la profondeur strategique immense.",
                "Placez des pierres sur les intersections du plateau. Entourez du territoire vide et capturez les groupes adverses en leur otant toutes leurs libertes.",
                "Le Go est associe aux arts savants de l'Asie orientale depuis plus de 2500 ans. Il est considere comme l'un des plus grands defis intellectuels jamais invente par l'homme.",
                "2 joueurs", "45-180 min", "10+", "~2500 av. J.-C.", "Tradition chinoise", "Done", false,
                "Territory, liberties, capture, ko rule",
                List.of("Weiqi", "Baduk"),
                List.of("Board", "Space", "Territory"),
                List.of("territory", "liberties", "capture"),
                List.of("Classical East Asian strategy game"),
                tutorial("Poser une pierre", "Les joueurs posent une pierre a tour de role sur une intersection vide.",
                        List.of(". . . . .", ". . . . .", ". . . . ."), List.of(". . . . .", ". . B . .", ". . . . .")),
                tutorial("Entourer un territoire", "Des pierres qui encerclent des intersections vides les comptent comme territoire.",
                        List.of(". B B B .", "B . . . B", ". B B B ."), List.of(". B B B .", "B T T T B", ". B B B ."))));

        // 5 — Chess
        games.add(game(5L, "Echecs", "Asie", "Inde / Perse / Europe", "Medieval", "Strategie", "Amateur",
                "Jeu de strategie abstraite ou deux armees s'affrontent jusqu'a la capture du roi adverse.",
                "Chaque piece se deplace selon des regles specifiques. L'objectif est de mettre le roi adverse en echec et mat, c'est-a-dire sans issue possible.",
                "Les Echecs naissent du Chaturanga indien, traversent la Perse sous le nom de Shatranj, et arrivent en Europe via le monde arabe au Xe siecle. Ils sont devenus le jeu de strategie universel par excellence.",
                "2 joueurs", "30-90 min", "8+", "~600 ap. J.-C.", "Tradition indienne", "Done", false,
                "Checkmate, piece movement, royal threat",
                List.of("Chess", "Chaturanga", "Shatranj"),
                List.of("Board", "War", "Checkmate"),
                List.of("checkmate", "rook", "bishop", "king"),
                List.of("Chess-family historical development"),
                tutorial("Mouvement de la tour", "La tour se deplace en ligne droite horizontalement ou verticalement.",
                        List.of(". . . .", ". R . .", ". . . .", ". . . ."), List.of(". . . .", ". . . R", ". . . .", ". . . .")),
                tutorial("Echec et mat", "Le roi adverse est mis en echec sans pouvoir s'echapper.",
                        List.of(". . R .", ". . . .", ". . k .", ". K . ."), List.of(". . R .", ". . R .", ". . k .", ". K . ."))));

        // 6 — Dames
        games.add(game(6L, "Dames", "Europe", "France / Egypte", "Medieval", "Strategie", "Debutant",
                "Jeu de prise diagonale sur un damier, accessible et tactique.",
                "Avancez en diagonale case par case. La prise est obligatoire : sautez par-dessus une piece adverse pour la capturer. Atteignez la rangee du fond pour devenir Dame.",
                "Les Dames modernes combinent le plateau du jeu d'Alquerque arabe et le damier medieval europeen. Le jeu s'est repandu en France au XIIe siecle.",
                "2 joueurs", "20-45 min", "6+", "~1100 ap. J.-C.", "Tradition europeenne", "Done", false,
                "Diagonal movement, leaping capture, promotion",
                List.of("Draughts", "Checkers"),
                List.of("Board", "War", "Leaping"),
                List.of("leaping", "diagonal", "promotion"),
                List.of("Draughts/checkers family"),
                tutorial("Avancer en diagonale", "Une piece normale avance d'une case en diagonale vers l'avant.",
                        List.of(". . . .", ". A . .", ". . . .", ". . . ."), List.of(". . . .", ". . . .", ". . A .", ". . . .")),
                tutorial("Capturer en sautant", "Sautez par-dessus une piece adverse adjacente pour la supprimer.",
                        List.of(". . . .", ". A . .", ". . B .", ". . . ."), List.of(". . . .", ". . . .", ". . . .", ". . . A"))));

        // 7 — Shogi
        games.add(game(7L, "Shogi", "Asie", "Japon", "Medieval", "Strategie", "Passionne",
                "Echecs japonais ou les pieces capturees peuvent etre remises en jeu par le capteur.",
                "Mettez le roi adverse en echec et mat. Les pieces capturees rejoignent votre main et peuvent etre larguees sur n'importe quelle case vide, ce qui donne une profondeur unique.",
                "Le Shogi s'est developpe au Japon a partir du Xe siecle avec la regle de parachutage unique. C'est l'un des jeux de strategie les plus complexes du monde.",
                "2 joueurs", "30-180 min", "10+", "~1000 ap. J.-C.", "Tradition japonaise", "Done", false,
                "Drops, checkmate, promotion",
                List.of("Japanese Chess"),
                List.of("Board", "War", "Replacement"),
                List.of("drop", "promotion", "checkmate"),
                List.of("Japanese chess-family game"),
                tutorial("Capturer une piece", "Une piece capturee rejoint la main du capteur.",
                        List.of(". . .", ". P e", ". . ."), List.of(". . .", ". . P", "Main: e")),
                tutorial("Larguer depuis la main", "Une piece en main peut etre posee sur n'importe quelle case vide.",
                        List.of(". . .", ". . P", "Main: e"), List.of(". E .", ". . P", "Main: vide"))));

        // 8 — Tablut
        games.add(game(8L, "Tablut", "Europe", "Scandinavie", "Medieval", "Strategie", "Amateur",
                "Jeu asymetrique de la famille Tafl : un roi tente de s'echapper, ses adversaires l'en empechent.",
                "Le roi cherche a atteindre un bord du plateau. Les attaquants en surnombre doivent l'encercler et le capturer par flanquement.",
                "Le Tablut a ete documente en Laponie par Carl von Linne au XVIIIe siecle lors d'un voyage en Scandinavie. C'est le representant le mieux documente de la famille Tafl.",
                "2 joueurs", "30-45 min", "10+", "~400 ap. J.-C.", "Tradition nordique", "Done", false,
                "Custodial capture, escape, asymmetric forces",
                List.of("Hnefatafl variant"),
                List.of("Board", "War", "Escape"),
                List.of("custodial-capture", "escape", "asymmetric"),
                List.of("Tafl game family"),
                tutorial("Objectif du roi", "Le roi gagne en atteignant une case de bord.",
                        List.of(". . . . .", ". . . . .", ". . K . .", ". . . . .", ". . . . ."),
                        List.of(". . K . .", ". . . . .", ". . . . .", ". . . . .", ". . . . .")),
                tutorial("Capture par flanquement", "Une piece est capturee quand deux pieces adverses l'encadrent.",
                        List.of(". . .", "A D .", ". . ."), List.of(". . .", "A D A", ". . ."))));

        // 9 — Royal Game of Ur
        games.add(game(9L, "Jeu Royal d'Ur", "Asie", "Mesopotamie (Irak)", "Ancient", "Hasard et strategie", "Debutant",
                "Jeu de course mesopotamien retrouve dans les tombes royales d'Ur, l'un des plus anciens jeux du monde.",
                "Lancez des pyramides de jet, avancez vos pions le long du parcours. Les cases rosettes octroient un tour supplementaire. Le premier a sortir tous ses pions gagne.",
                "Des plateaux du Jeu d'Ur datent d'environ 2600 av. J.-C. Le reglement complet a ete retrouve grave sur une tablette cuneiforme babylonienne traduite par Irving Finkel en 1983.",
                "2 joueurs", "15-30 min", "7+", "~2600 av. J.-C.", "Mesopotamie ancienne", "Done", false,
                "Race, rosette squares, throw sticks",
                List.of("Game of Twenty Squares", "Jeu des Vingt Cases"),
                List.of("Board", "Race", "Dice"),
                List.of("race", "rosette", "dice"),
                List.of("Royal Tombs of Ur - British Museum collection"),
                tutorial("La case rosette", "Atterrir sur une rosette donne un tour supplementaire.",
                        List.of("A . * . ."), List.of(". . * A .")),
                tutorial("Sortir du plateau", "Le pion sort apres avoir parcouru tout le trajet.",
                        List.of(". . . A exit"), List.of(". . . . A"))));

        // 10 — Xiangqi
        games.add(game(10L, "Xiangqi", "Asie", "Chine", "Medieval", "Strategie", "Passionne",
                "Echecs chinois avec un palais, une riviere separatrice et un canon qui capture en sautant.",
                "Protegez votre general dans son palais. Le canon capture en sautant exactement une piece intermediaire. La riviere centrale restreint certains mouvements.",
                "Le Xiangqi est l'un des jeux de strategie les plus pratiques au monde, avec plusieurs centaines de millions de joueurs. Il s'est developpe en Chine a partir du VIIe siecle.",
                "2 joueurs", "30-90 min", "8+", "~700 ap. J.-C.", "Tradition chinoise", "Done", false,
                "Palace, river, cannon capture",
                List.of("Chinese Chess", "Jiangjun"),
                List.of("Board", "War", "Checkmate"),
                List.of("cannon", "river", "palace"),
                List.of("Chinese chess-family game"),
                tutorial("Capture du canon", "Le canon saute exactement par-dessus une piece intermediaire pour capturer.",
                        List.of("C . S . e"), List.of(". . S . C")),
                tutorial("General dans le palais", "Le general reste conline a l'interieur du palais.",
                        List.of("p p p", "p G p", "p p p"), List.of("p G p", "p . p", "p p p"))));

        // 11 — Carrom
        games.add(game(11L, "Carrom", "Asie", "Inde / Sri Lanka", "Modern", "Adresse", "Debutant",
                "Jeu d'adresse ou l'on propulse des palets en bois dans les poches d'un plateau carre.",
                "Frappez le striker avec l'index pour envoyer les pieces de votre couleur dans les quatre poches d'angle. Empochee la reine pour un bonus. Le premier a vider son camp gagne.",
                "Tres populaire en Asie du Sud et dans sa diaspora, le Carrom est souvent appele le billard des doigts. Il est joue aussi bien en famille que dans des competitions internationales.",
                "2-4 joueurs", "15-30 min", "6+", "~1800 ap. J.-C.", "Asie du Sud", "Done", false,
                "Flick, pockets, queen",
                List.of("Billard des doigts"),
                List.of("Board", "Dexterity", "Pocket"),
                List.of("flick", "pocket", "queen"),
                List.of("International Carrom Federation rules"),
                tutorial("Viser le striker", "Le striker est aligne pour frapper un palet.",
                        List.of("coin . coin", ". p .", ". S ."), List.of("coin . coin", ". p .", ". S ->")),
                tutorial("Empocher un palet", "Un bon tir envoie le palet dans une poche de coin.",
                        List.of("P . .", ". p .", ". S ."), List.of("P . .", ". . .", "p in pocket"))));

        // 12 — Fanorona
        games.add(game(12L, "Fanorona", "Afrique", "Madagascar", "Medieval", "Strategie", "Amateur",
                "Jeu national de Madagascar avec un systeme de capture unique : par approche ou par retrait.",
                "Deplacez une piece vers des pieces adverses (capture par approche) ou eloignez-la d'elles (capture par retrait). Une sequence de captures multiples est possible en un seul tour.",
                "Le Fanorona est intrinsequement lie a l'identite malgache. La legende dit que le roi Ralambo y jouait au XVIIe siecle. Il est classe patrimoine culturel a Madagascar.",
                "2 joueurs", "20-40 min", "8+", "~1680 ap. J.-C.", "Madagascar", "Done", false,
                "Approach capture, withdrawal capture, sequence",
                List.of("Fanorona-Tsivy"),
                List.of("Board", "War", "Line"),
                List.of("approach", "withdrawal", "capture"),
                List.of("Malagasy traditional game"),
                tutorial("Capture par approche", "Avancer vers des pieces adverses adjacentes les capture toutes.",
                        List.of("A . B B B", ". . . . ."), List.of(". A . . .", ". . . . .")),
                tutorial("Capture par retrait", "Reculer d'une piece adverse adjacente la capture.",
                        List.of("B B B A .", ". . . . ."), List.of(". . . . A", ". . . . ."))));

        // 13 — Surakarta
        games.add(game(13L, "Surakarta", "Asie", "Indonesie (Java)", "Modern", "Strategie", "Amateur",
                "Jeu javanais ou les captures s'effectuent en suivant des boucles circulaires aux coins du plateau.",
                "Deplacez normalement d'une case adjacente. Pour capturer, une piece doit parcourir au moins une boucle avant d'atteindre la piece adverse. Eliminez toutes les pieces ennemies.",
                "Originaire de la ville de Surakarta (Solo) a Java, ce jeu est connu pour son systeme de capture unique base sur les boucles. Il est enseigne dans les ecoles indonesiennes.",
                "2 joueurs", "15-30 min", "8+", "~1500 ap. J.-C.", "Java, Indonesie", "Done", false,
                "Loop capture, orthogonal movement",
                List.of("Solo"),
                List.of("Board", "War", "Loop"),
                List.of("loop", "capture", "orthogonal"),
                List.of("Javanese traditional board game"),
                tutorial("Deplacement normal", "Une piece se deplace d'une case vers un point adjacent vide.",
                        List.of(". A .", ". . .", ". . ."), List.of(". . .", ". A .", ". . .")),
                tutorial("Capture en boucle", "Le chemin de capture doit traverser au moins une boucle de coin.",
                        List.of("A . . O . B"), List.of(". . . O . A"))));

        // 14 — Moulin (Nine Men's Morris)
        games.add(game(14L, "Moulin", "Europe", "Mediterranee / Afrique du Nord", "Ancient", "Strategie", "Debutant",
                "Jeu de formation lineaire : alignez 3 pieces pour former un moulin et capturer une piece adverse.",
                "Phase 1 - Pose : placez vos 9 pieces sur les noeuds du plateau. Phase 2 - Deplacement : glissez une piece vers un noeud adjacent. Chaque moulin (3 en ligne) permet de supprimer une piece adverse.",
                "Retrouve grave dans les temples de Louxor et dans des sites romains, le Moulin est l'un des jeux de strategie les plus repandus de l'Antiquite. Il est joue dans presque toutes les cultures du monde.",
                "2 joueurs", "20-30 min", "6+", "~1400 av. J.-C.", "Tradition mediterraneenne", "Done", false,
                "Mill formation, three-in-line, capture on mill",
                List.of("Nine Men's Morris", "Merels", "Muhle"),
                List.of("Board", "War", "Alignment", "Line"),
                List.of("mill", "alignment", "three-in-line", "capture"),
                List.of("Found in ancient Luxor temple and Roman military sites"),
                tutorial("Former un moulin", "Aligner 3 pieces permet de capturer immediatement une piece adverse.",
                        List.of("A . .", ". A .", ". . ."), List.of("A . .", ". A .", ". A .")),
                tutorial("Rouvrir un moulin", "Sortir puis rentrer dans un moulin permet de capturer a chaque fois.",
                        List.of("A A A", ". . .", ". . B"), List.of("A A .", ". . A", ". . B"))));

        // 15 — Alquerque
        games.add(game(15L, "Alquerque", "Afrique", "Monde arabe / Al-Andalus", "Medieval", "Strategie", "Debutant",
                "Ancetre direct des Dames, ce jeu arabe utilise des captures par saut sur une grille de 25 points.",
                "Deplacez vos pieces le long des lignes vers des points adjacents. Sautez par-dessus une piece adverse pour la capturer. La capture est obligatoire si possible.",
                "Documente dans le Libro de los Juegos du roi Alphonse X de Castille (1283), l'Alquerque est d'origine arabe. Il est le precurseur direct des Dames modernes.",
                "2 joueurs", "15-25 min", "7+", "~1000 ap. J.-C.", "Monde arabe", "Done", false,
                "Line movement, leaping capture, mandatory capture",
                List.of("El-quirkat"),
                List.of("Board", "War", "Leaping"),
                List.of("leaping", "mandatory-capture", "diagonal"),
                List.of("Libro de los Juegos (1283) - Alfonso X of Castile"),
                tutorial("Deplacer sur une ligne", "Les pieces se deplacent le long des lignes vers des points adjacents vides.",
                        List.of("A . . . .", ". . B . .", ". . . . ."), List.of(". A . . .", ". . B . .", ". . . . .")),
                tutorial("Capturer par saut", "Sautez par-dessus un adversaire adjacent pour le capturer.",
                        List.of(". . . . .", ". A B . .", ". . . . ."), List.of(". . . . .", ". . . A .", ". . . . ."))));

        // 16 — Toguz Korgool
        games.add(game(16L, "Toguz Korgool", "Asie", "Asie Centrale (Kirghizstan / Kazakhstan)", "Ancient", "Strategie", "Amateur",
                "Jeu de semailles d'Asie centrale avec neuf trous par joueur et une caisse de capture centrale.",
                "Semez les pierres depuis un trou. Si la derniere pierre tombe dans un trou adverse contenant un nombre pair de pierres, le joueur capture ce trou dans sa caisse. Le joueur avec le plus de pierres gagne.",
                "Traditionnel chez les peuples kirghiz, kazakh et kalmyk, le Toguz Korgool fait partie du patrimoine culturel immateriel de l'UNESCO. Il est inscrit comme tradition nationale au Kirghizstan.",
                "2 joueurs", "20-40 min", "8+", "~500 ap. J.-C.", "Tradition kirghize", "Done", false,
                "Sow, capture on even, kazan (treasury)",
                List.of("Toguz Kumalak", "Toguz Korganool"),
                List.of("Board", "Sow", "Store"),
                List.of("sow", "even-capture", "kazan"),
                List.of("UNESCO intangible cultural heritage of Kyrgyzstan"),
                tutorial("Semer depuis un trou", "Ramassez toutes les pierres d'un trou et semez-les une par une.",
                        List.of("9 9 9 9 9 9 9 9 9 | K=0", "9 9 9 9 9 9 9 9 9 | K=0"),
                        List.of("9 9 9 9 9 9 9 0 9 | K=0", "9 10 10 10 10 10 10 10 10 | K=0")),
                tutorial("Capturer un trou pair", "Si la derniere graine tombe dans un trou adverse a compte pair, vous capturez ce trou.",
                        List.of("9 9 9 9 9 9 9 9 9 | K=0", "9 9 8 9 9 9 9 9 9 | K=0"),
                        List.of("9 9 9 9 9 9 9 9 9 | K=0", "9 9 0 9 9 9 9 9 9 | K=9"))));

        // 17 — Bagh Chal
        games.add(game(17L, "Bagh Chal", "Asie", "Nepal", "Ancient", "Strategie", "Amateur",
                "Jeu de chasse asymetrique : 4 tigres tentent de capturer 5 chevres, les chevres cherchent a bloquer les tigres.",
                "Les chevres sont d'abord placees une par une sur le plateau. Les tigres se deplacent d'une intersection et capturent en sautant par-dessus une chevre. Les chevres bloquent les tigres en remplissant les intersections.",
                "Jeu traditionnel du Nepal, le Bagh Chal (Tigres et Chevres en nepali) est l'un des jeux de chasse asymetriques les plus connus d'Asie du Sud. Il se joue sur un plateau de 5x5 points.",
                "2 joueurs", "20-30 min", "8+", "Antiquite", "Tradition nepalaise", "Done", false,
                "Hunt game, asymmetric, blocking vs capturing",
                List.of("Tigers and Goats", "Bagha Chal"),
                List.of("Board", "Hunt", "Asymmetric"),
                List.of("hunt", "asymmetric", "blocking", "capture"),
                List.of("Traditional Nepalese board game"),
                tutorial("Placer une chevre", "Au debut, les chevres sont posees une par une sur les intersections.",
                        List.of(". . . . .", ". . . . .", ". . . . .", ". . . . .", ". . . . ."),
                        List.of(". . . . .", ". G . . .", ". . . . .", ". . . . .", ". . . . .")),
                tutorial("Tigre capture", "Un tigre saute par-dessus une chevre adjacente vers une case vide.",
                        List.of(". . . . .", ". T G . .", ". . . . ."), List.of(". . . . .", ". . . T .", ". . . . ."))));

        // 18 — Yoté
        games.add(game(18L, "Yote", "Afrique", "Afrique de l'Ouest (Senegal / Mali)", "Ancient", "Strategie", "Debutant",
                "Jeu ouest-africain ou les pieces sont placees progressivement et les captures sont strategiques.",
                "Posez ou deplacez une piece par tour sur la grille 5x6. Capturez en sautant par-dessus un adversaire. Bonus : apres une capture, vous pouvez retirer une piece adverse supplementaire.",
                "Traditionnel au Senegal, au Mali et en Cote d'Ivoire, le Yote se joue sur une grille tracee dans le sable avec des graines ou des cailloux. Son systeme de capture bonus le rend unique.",
                "2 joueurs", "15-30 min", "6+", "Antiquite", "Tradition ouest-africaine", "Done", false,
                "Placement, capture, bonus removal",
                List.of("Yoote"),
                List.of("Board", "War", "Leaping"),
                List.of("placement", "leaping", "bonus-capture"),
                List.of("West African traditional game"),
                tutorial("Placer ou deplacer", "Par tour, placez une nouvelle piece OU deplacez une piece existante.",
                        List.of(". . . . .", ". . . . .", ". . . . ."), List.of(". . . . .", ". A . . .", ". . . . .")),
                tutorial("Capture et bonus", "Apres avoir capture, retirez aussi une piece adverse de votre choix.",
                        List.of(". A B . .", ". . . . ."), List.of(". . . A .", ". . . . ."))));

        // 19 — Pachisi
        games.add(game(19L, "Pachisi", "Asie", "Inde", "Medieval", "Hasard et strategie", "Debutant",
                "Jeu de course indien en croix sur lequel les joueurs avancent selon les lancers de cauris.",
                "Lancez les cauris (coquillages) pour determiner vos deplacements. Faites le tour du plateau en croix et ramenez toutes vos pieces au centre. Les pieces adverses peuvent etre renvoyees au depart.",
                "Le Pachisi est l'ancetre direct du Ludo moderne. Des cours de Pachisi grandeur nature etaient construites dans les palais moghols. L'Akbar bar jouait avec des esclaves comme pions vivants.",
                "2-4 joueurs", "30-60 min", "7+", "~4e siecle ap. J.-C.", "Tradition moghole / indienne", "Done", false,
                "Race, cross track, cowrie shells, home base",
                List.of("Chaupar", "Ludo ancestor"),
                List.of("Board", "Race", "Dice"),
                List.of("race", "cross-track", "cowrie", "home-base"),
                List.of("Mughal imperial court records"),
                tutorial("Lancer les cauris", "Le nombre de cauris face visible determine le deplacement.",
                        List.of("Cauris: 3 face", "Avancer de 3 cases"), List.of("A . . . .", ". . . A .")),
                tutorial("Rentrer au centre", "Ramener toutes ses pieces au centre pour gagner.",
                        List.of("A . . . .", ". . . . ."), List.of(". . . . .", ". . . A ."))));

        // 20 — Hnefatafl
        games.add(game(20L, "Hnefatafl", "Europe", "Scandinavie / Iles Britanniques", "Ancient", "Strategie", "Amateur",
                "Jeu de guerre nordique asymetrique : le roi cherche a atteindre un coin, les assaillants cherchent a le capturer.",
                "Le roi doit atteindre l'un des quatre coins. Les assaillants, deux fois plus nombreux, capturent les pieces adverses par flanquement. Le roi est capture quand il est encercle sur ses quatre cotes.",
                "Joue par les Vikings du IVe au XIIe siecle avant d'etre supplante par les Echecs, le Hnefatafl est documente dans des sagas nordiques. Des pieces en ambre et en os ont ete retrouvees en Scandinavie.",
                "2 joueurs", "30-60 min", "10+", "~400 ap. J.-C.", "Tradition viking", "Done", false,
                "Custodial capture, king escape, asymmetric armies",
                List.of("King's Table", "Tawlbwrdd"),
                List.of("Board", "War", "Escape"),
                List.of("custodial-capture", "king-escape", "asymmetric"),
                List.of("Norse sagas, archaeological finds in Scandinavia"),
                tutorial("Le roi vers le coin", "Le roi gagne en atteignant un coin du plateau.",
                        List.of("+ . . . +", ". . . . .", ". . K . .", ". . . . .", "+ . . . +"),
                        List.of("K . . . +", ". . . . .", ". . . . .", ". . . . .", "+ . . . +")),
                tutorial("Flanquement", "Une piece est capturee quand deux ennemis l'encadrent sur une meme ligne.",
                        List.of("E . A . E"), List.of("E . . . E"))));

        return games;
    }

    private Game game(Long id, String name, String region, String country, String period, String type, String difficulty,
                      String description, String rules, String history, String players, String duration, String age,
                      String year, String author, String reconstructionStatus, boolean playable, String ludemeSummary,
                      List<String> aliases, List<String> categories, List<String> ludemes, List<String> references,
                      GameTutorialStep... tutorialSteps) {
        return new Game(id, name, region, country, period, type, difficulty, description, rules, history,
                "", players, duration, age, year, author, reconstructionStatus, playable, ludemeSummary,
                aliases, categories, ludemes, references, List.of(tutorialSteps));
    }

    private GameTutorialStep tutorial(String title, String explanation, List<String> beforeBoard, List<String> afterBoard) {
        return new GameTutorialStep(
                title,
                "Comprendre ce mouvement avant d'apprendre la partie complete.",
                explanation + " Observez d'abord le plateau Avant, puis comparez avec l'Apres. Les cases modifiees montrent le resultat du mouvement.",
                "Chaque cellule est un point du plateau simplifie. Les lettres sont des pieces, les chiffres des graines, les points des espaces vides.",
                "Le plateau Apres montre l'etat exact apres le mouvement exemple.",
                "Avant de cliquer Suivant, dites en une phrase ce qui a change entre les deux plateaux.",
                List.of("A = joueur courant", "B/e/D/k/G/T/E = piece adversaire ou cible", ". = vide", "nombres = graines", "* ou + = case speciale"),
                beforeBoard,
                afterBoard
        );
    }
}
