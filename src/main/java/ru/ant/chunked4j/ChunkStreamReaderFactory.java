package ru.ant.chunked4j;

public interface ChunkStreamReaderFactory<R extends ChunkStreamReader> {
    R create(ChunkInputStream stream);
}
