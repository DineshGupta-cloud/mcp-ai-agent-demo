package com.example.aiagent.controller;

import com.example.aiagent.dto.ChatRequest;
import com.example.aiagent.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    private final ChatClient chatClient;

    public AgentController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody(required = false) ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            log.warn("Invalid chat request received: message is empty or null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ChatResponse("Invalid request: message cannot be empty."));
        }

        String userMessage = request.getMessage().trim();
        log.info("Incoming chat request: userMessage='{}'", userMessage);

        try {
            log.debug("Dispatching request to Spring AI ChatClient...");
            String aiAnswer = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            log.info("AI response received successfully");
            return ResponseEntity.ok(new ChatResponse(aiAnswer));

        } catch (Exception ex) {
            log.error("Error occurred while processing AI request with message: {}", userMessage, ex);

            String message = ex.getMessage() != null ? ex.getMessage() : "";
            if (message.contains("mcp") || message.contains("Connection refused") || message.contains("Failed to connect")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ChatResponse("MCP business tools server is currently unavailable. Please verify the MCP server is running."));
            } else if (message.contains("401") || message.contains("Incorrect API key") || message.contains("OPENAI_API_KEY") || message.contains("OPENROUTER_API_KEY") || message.contains("User not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ChatResponse("Authentication failed. Please verify your OPENROUTER_API_KEY or OPENAI_API_KEY environment variable."));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("An unexpected error occurred while communicating with the AI service. Please try again."));
        }
    }
}
