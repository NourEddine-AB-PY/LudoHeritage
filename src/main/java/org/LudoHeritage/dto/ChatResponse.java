package org.LudoHeritage.dto;

import org.LudoHeritage.model.Game;

import java.util.List;

public record ChatResponse(String reply, List<String> reasoning, List<Game> games) {
}
