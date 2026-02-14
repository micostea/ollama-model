CREATE TABLE IF NOT EXISTS vector_store (
                                            id TEXT PRIMARY KEY,
                                            content TEXT,
                                            metadata JSONB,
                                            embedding VECTOR(768)
    );