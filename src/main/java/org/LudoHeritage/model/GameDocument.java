package org.LudoHeritage.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "games")
public class GameDocument {
    @Id
    private Long id;
    private String name;
    private String region;
    private String country;
    private String period;
    private String type;
    private String difficulty;
    private String description;
    private String rules;
    private String history;
    private String imageUrl;
    private String players;
    private String duration;
    private String age;
    private String year;
    private String author;
    private String reconstructionStatus;
    private boolean playable;
    private String ludemeSummary;
    private List<String> aliases = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<String> ludemes = new ArrayList<>();
    private List<String> references = new ArrayList<>();
    private List<GameTutorialStep> tutorialSteps = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPlayers() { return players; }
    public void setPlayers(String players) { this.players = players; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getReconstructionStatus() { return reconstructionStatus; }
    public void setReconstructionStatus(String reconstructionStatus) { this.reconstructionStatus = reconstructionStatus; }
    public boolean isPlayable() { return playable; }
    public void setPlayable(boolean playable) { this.playable = playable; }
    public String getLudemeSummary() { return ludemeSummary; }
    public void setLudemeSummary(String ludemeSummary) { this.ludemeSummary = ludemeSummary; }
    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
    public List<String> getLudemes() { return ludemes; }
    public void setLudemes(List<String> ludemes) { this.ludemes = ludemes; }
    public List<String> getReferences() { return references; }
    public void setReferences(List<String> references) { this.references = references; }
    public List<GameTutorialStep> getTutorialSteps() { return tutorialSteps; }
    public void setTutorialSteps(List<GameTutorialStep> tutorialSteps) { this.tutorialSteps = tutorialSteps; }

    public static GameDocument fromGame(Game game) {
        GameDocument doc = new GameDocument();
        doc.setId(game.id());
        doc.setName(game.name());
        doc.setRegion(game.region());
        doc.setCountry(game.country());
        doc.setPeriod(game.period());
        doc.setType(game.type());
        doc.setDifficulty(game.difficulty());
        doc.setDescription(game.description());
        doc.setRules(game.rules());
        doc.setHistory(game.history());
        doc.setImageUrl(game.imageUrl());
        doc.setPlayers(game.players());
        doc.setDuration(game.duration());
        doc.setAge(game.age());
        doc.setYear(game.year());
        doc.setAuthor(game.author());
        doc.setReconstructionStatus(game.reconstructionStatus());
        doc.setPlayable(game.playable());
        doc.setLudemeSummary(game.ludemeSummary());
        doc.setAliases(game.aliases());
        doc.setCategories(game.categories());
        doc.setLudemes(game.ludemes());
        doc.setReferences(game.references());
        doc.setTutorialSteps(game.tutorialSteps());
        return doc;
    }

    public Game toGame() {
        return new Game(id, name, region, country, period, type, difficulty, description, rules, history,
                imageUrl, players, duration, age, year, author, reconstructionStatus, playable,
                ludemeSummary, safe(aliases), safe(categories), safe(ludemes), safe(references), safeTutorials(tutorialSteps));
    }

    private List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    private List<GameTutorialStep> safeTutorials(List<GameTutorialStep> values) {
        return values == null ? List.of() : values;
    }
}
