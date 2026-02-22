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

        TokenTextSplitter splitter = new TokenTextSplitter(200, 50, 50, 20, false);

        for (MultipartFile file : files) {

            // Extract text with Tika
            TikaTikaDocumentReader reader = new TikaTikaDocumentReader(file.getInputStream());
            List<Document> docs = reader.get(); // usually 1 document

            for (Document page : docs) {

                String text = page.getText();
                if (text == null || text.isBlank()) continue;

                // 1) Try splitting by Tika page delimiter
                String[] pages = text.split("\\f");

                // 2) Fallback: if Tika did NOT split anything, split manually
                if (pages.length == 1) {
                    pages = text.split("(?<=\\G.{3000})"); // split every ~3000 chars
                }

                // 3) Process each page
                for (String p : pages) {
                    if (p == null || p.isBlank()) continue;

                    List<Document> chunks = splitter.split(new Document(p));

                    // Add metadata to each chunk
                    for (Document chunk : chunks) {
                        chunk.getMetadata().put("filename", file.getOriginalFilename());
                    }

                    // Store chunks in vector DB
                    vectorStore.add(chunks);
                }
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