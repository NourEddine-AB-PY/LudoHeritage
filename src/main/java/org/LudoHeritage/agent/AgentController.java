package org.LudoHeritage.agent;

import org.LudoHeritage.dto.ChatResponse;
import org.LudoHeritage.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = agentService.chat(
            request.getUserId(),
            request.getMessage(),
            request.getHistory(),
            request.getProfile()
        );
        return ResponseEntity.ok(response);
    }
}
