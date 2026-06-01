package org.LudoHeritage.quiz;

public record SubmitScoreRequest(String userId, String displayName, int score, int total) {}
