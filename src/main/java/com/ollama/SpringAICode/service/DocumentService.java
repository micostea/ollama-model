package com.ollama.SpringAICode.service;

import com.ollama.SpringAICode.reader.TikaTikaDocumentReader;
import org.springframework.ai.document.Document;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void indexPdf(MultipartFile file) {
        try {
            // 1. Citește PDF-ul
            TikaTikaDocumentReader reader = new TikaTikaDocumentReader(file.getInputStream());
            List<Document> docs = reader.get();

            // 2. Chunking
            TokenTextSplitter splitter = new TokenTextSplitter(
                    500,   // max tokens per chunk
                    50,    // min tokens
                    1000,  // max chunk size (în caractere)
                    50,    // overlap
                    false  // keep separator
            );
            List<Document> chunks = splitter.split(docs);

            // 3. Trimite chunk-urile în vector store
            vectorStore.add(chunks);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF", e);
        }
    }

}