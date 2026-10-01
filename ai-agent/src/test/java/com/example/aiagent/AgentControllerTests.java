package com.example.aiagent;

import com.example.aiagent.controller.AgentController;
import com.example.aiagent.dto.ChatRequest;
import com.example.aiagent.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AgentControllerTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private AgentController agentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agentController = new AgentController(chatClient);
    }

    @Test
    void testChatEmptyMessageReturnsBadRequest() {
        ResponseEntity<ChatResponse> response = agentController.chat(new ChatRequest(""));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponse()).contains("cannot be empty");
    }

    @Test
    void testChatNullRequestReturnsBadRequest() {
        ResponseEntity<ChatResponse> response = agentController.chat(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponse()).contains("cannot be empty");
    }

    @Test
    void testChatSuccessfulResponse() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("Employee 1 is Alice Johnson, Senior Java Engineer.");

        ResponseEntity<ChatResponse> response = agentController.chat(new ChatRequest("Who is employee 1?"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponse()).isEqualTo("Employee 1 is Alice Johnson, Senior Java Engineer.");
    }

    @Test
    void testChatMcpErrorHandling() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("Connection refused to mcp endpoint"));

        ResponseEntity<ChatResponse> response = agentController.chat(new ChatRequest("Who is employee 1?"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponse()).contains("MCP business tools server is currently unavailable");
    }

    @Test
    void testChatOpenAiAuthErrorHandling() {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("Incorrect API key provided (401)"));

        ResponseEntity<ChatResponse> response = agentController.chat(new ChatRequest("Hello"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponse()).contains("OPENAI_API_KEY");
    }
}
