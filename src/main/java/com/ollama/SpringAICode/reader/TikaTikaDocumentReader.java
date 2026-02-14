package com.ollama.SpringAICode.reader;

import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;

import java.io.InputStream;
import java.util.List;

public class TikaTikaDocumentReader implements DocumentReader {

    private final InputStream inputStream;

    public TikaTikaDocumentReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public List<Document> get() {
        try {
            Tika tika = new Tika();
            String text = tika.parseToString(inputStream);

            // 🔧 Remove NULL bytes (0x00) and other control characters
            text = text.replace("\u0000", "");
            text = text.replaceAll("\\p{C}", "");

            // 🔍 Optional: skip empty or garbage documents
            if (text.trim().isEmpty()) {
                return List.of();
            }

            return List.of(new Document(text));

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}