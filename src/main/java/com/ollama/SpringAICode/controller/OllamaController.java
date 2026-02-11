package com.ollama.SpringAICode.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ollama")
public class OllamaController {

    @GetMapping("/ask")
    public String ask() {
        return "The /ollama/ask endpoint is working!";
    }
}

