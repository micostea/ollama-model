package com.ollama.SpringAICode.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ollama/chat")
public class OllamaChatController {

    private final ChatClient chatClient;

    public OllamaChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public String chat(@RequestBody String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}

