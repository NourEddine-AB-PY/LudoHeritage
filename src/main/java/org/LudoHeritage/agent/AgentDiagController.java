package org.LudoHeritage.agent;

import org.LudoHeritage.service.OllamaChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentDiagController {

    private final OllamaChatClient llm;

    public AgentDiagController(OllamaChatClient llm) {
        this.llm = llm;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(llm.ping());
    }
}
