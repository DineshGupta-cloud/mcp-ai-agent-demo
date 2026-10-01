package com.example.aiagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatConfig.class);

    public static final String SYSTEM_PROMPT = """
            You are a business assistant.

            You have access to business tools through MCP.

            Use MCP tools whenever the user's question requires
            information that can be retrieved using those tools.

            Never invent employee information.

            When a tool returns data, use that data to formulate
            the answer.

            For mathematical questions, use the calculator tools
            instead of calculating manually when appropriate.

            Keep responses concise and clear.
            """;

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ObjectProvider<SyncMcpToolCallbackProvider> mcpToolCallbackProvider) {
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT);

        SyncMcpToolCallbackProvider provider = mcpToolCallbackProvider.getIfAvailable();
        if (provider != null) {
            log.info("MCP ToolCallbackProvider detected. Registering MCP tools with ChatClient...");
            builder.defaultToolCallbacks(provider.getToolCallbacks());
        } else {
            log.warn("No MCP ToolCallbackProvider found. ChatClient configured without MCP tools.");
        }

        return builder.build();
    }
}
