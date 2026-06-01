package org.LudoHeritage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class OllamaChatClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaChatClient.class);

    private final WebClient webClient;
    private final String configuredModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaChatClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:}") String configuredModel) {
        this.configuredModel = configuredModel;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
        log.info("LudoBot — Ollama client ready  url={}  model={}",
                baseUrl, configuredModel.isBlank() ? "auto-detect" : configuredModel);
    }

    public String chat(String systemMessage, String userMessage) {
        return chat(systemMessage, userMessage, 0.4, 400);
    }

    public String chat(String systemMessage, String userMessage, double temperature, int maxTokens) {
        try {
            String model = resolveModel();
            if (model == null || model.isBlank()) {
                log.warn("Aucun modele Ollama detecte. Lancez : ollama pull <model>");
                return null;
            }

            String prompt = systemMessage
                    + "\n\nREGLES STRICTES:"
                    + "\n- Tu es alimente par Ollama local."
                    + "\n- Reponds uniquement avec les jeux presents dans le contexte utilisateur."
                    + "\n- N'invente jamais un autre jeu."
                    + "\n- Si la question est hors sujet, dis clairement que tu es specialise dans les jeux de table."
                    + "\n\nUtilisateur: " + userMessage
                    + "\n\nLudoBot:";

            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", Map.of("temperature", temperature, "num_predict", maxTokens)
            );

            String responseJson = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseJson == null) return null;

            JsonNode root = objectMapper.readTree(responseJson);
            String response = root.path("response").asText("");
            return response.isBlank() ? null : response.trim();

        } catch (Exception ex) {
            log.error("Ollama chat failed: {} — {}", ex.getClass().getSimpleName(), ex.getMessage());
            return null;
        }
    }

    public Map<String, String> ping() {
        String model = resolveModel();
        if (model == null || model.isBlank()) {
            return Map.of("status", "error", "message", "Aucun modele Ollama installe. Lancez : ollama pull <model>");
        }
        String reply = chat("You are LudoBot.", "Say hello in French in one short sentence.");
        if (reply != null) {
            return Map.of("status", "ok", "provider", "ollama", "model", model, "reply", reply);
        }
        return Map.of("status", "error", "provider", "ollama", "model", model, "message", "Ollama ne repond pas");
    }

    private String resolveModel() {
        if (configuredModel != null && !configuredModel.isBlank()) return configuredModel;
        try {
            String responseJson = webClient.get().uri("/api/tags")
                    .retrieve().bodyToMono(String.class).block();
            if (responseJson == null) return null;
            JsonNode models = objectMapper.readTree(responseJson).path("models");
            if (models.isArray() && !models.isEmpty()) {
                return models.get(0).path("name").asText("");
            }
        } catch (Exception ex) {
            log.error("Impossible de lire les modeles Ollama: {}", ex.getMessage());
        }
        return null;
    }
}
