package com.ollama.SpringAICode.controller;


import com.ollama.SpringAICode.reader.TikaTikaDocumentReader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ollama/rag")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("files") List<MultipartFile> files) throws Exception {

        TokenTextSplitter splitter =
                new TokenTextSplitter(500, 50, 100, 1000, false);

        for (MultipartFile file : files) {

            // ✅ Use Tika to extract clean text from the PDF
            TikaTikaDocumentReader reader = new TikaTikaDocumentReader(file.getInputStream());
            List<Document> docs = reader.get(); // usually 1 document


            for (Document page : docs) {

                // Add metadata
                page.getMetadata().put("filename", file.getOriginalFilename());

                // Chunk the clean page text
                List<Document> chunks = splitter.split(page);

                // Store chunks in vector DB
                vectorStore.add(chunks);
            }
        }

        return "Documents uploaded successfully!";
    }

    @PostMapping("/ask")
    public String ask(@RequestBody String question) {

        List<Document> docs = vectorStore.similaritySearch(question);

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return chatClient
                .prompt()
                .system("Answer ONLY based on the following context:\n" + context)
                .user(question)
                .call()
                .content();
    }
}